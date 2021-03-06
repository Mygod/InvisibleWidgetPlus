package tk.mygod.invisibleWidgetPlus

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.{View, ViewGroup}
import android.widget.AdapterView.{OnItemClickListener, OnItemLongClickListener}
import android.widget._
import tk.mygod.app.{CircularRevealActivity, ToolbarActivity}
import tk.mygod.util.Conversions._
import tk.mygod.view.AnimationHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author Mygod
 */
class ShortcutsChooser extends ToolbarActivity
  with OnItemClickListener with OnItemLongClickListener {
  private final class ShortcutsListAdapter extends BaseAdapter {
    private val shortcuts = ShortcutsFetcher.getShortcuts(ShortcutsChooser.this)

    def getCount = shortcuts.size
    def getItem(position: Int) = shortcuts(position)
    def getItemId(position: Int) = position
    def getView(position: Int, convertView: View, parent: ViewGroup) = {
      val result = if (convertView == null) getLayoutInflater.inflate(R.layout.icon_list_item_2, parent, false)
        else convertView
      val manager = getPackageManager
      val info = shortcuts(position)
      result.findViewById(android.R.id.icon).asInstanceOf[ImageView].setImageDrawable(info.loadIcon(manager))
      result.findViewById(android.R.id.text1).asInstanceOf[TextView].setText(info.loadLabel(manager))
      result.findViewById(android.R.id.text2).asInstanceOf[TextView].setText(info.activityInfo.name)
      result
    }
  }

  private var adapter: ShortcutsListAdapter = _
  private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
  private var triggerOnDoubleTap: Boolean = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    val extras = getIntent.getExtras
    if (extras != null)
      widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish else {
      setContentView(R.layout.actions_chooser)
      configureToolbar()
      setNavigationIcon(R.drawable.ic_close)
      toolbar.inflateMenu(R.menu.actions_chooser)
      toolbar.getMenu.getItem(0).setOnMenuItemClickListener(item => {
        triggerOnDoubleTap = !item.isChecked
        item.setChecked(triggerOnDoubleTap)
        true
      })
      val list = findViewById(android.R.id.list).asInstanceOf[ListView]
      Future {
        adapter = new ShortcutsListAdapter
        runOnUiThread(() => {
          list.setAdapter(adapter)
          AnimationHelper.crossFade(ShortcutsChooser.this, findViewById(android.R.id.empty), list)
        })
      }
      list.setOnItemClickListener(this)
      list.setOnItemLongClickListener(this)
    }
  }

  def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
    val info = adapter.getItem(position).activityInfo
    val intent = new Intent(Intent.ACTION_CREATE_SHORTCUT).setClassName(info.packageName, info.name)
      .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
    if (info.packageName == getPackageName)
      startActivityForResult(CircularRevealActivity.putLocation(intent, getLocationOnScreen), position,
        InvisibleWidgetManager.makeLocalRevealAnimation(this, view))
    else startActivityForResult(intent, position, InvisibleWidgetManager.makeRevealAnimation(view))
  }

  def onItemLongClick(parent: AdapterView[_], view: View, position: Int, id: Long) = {
    try startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
      .setData("package:" + adapter.getItem(position).activityInfo.packageName),
      InvisibleWidgetManager.makeRevealAnimation(view)) catch {
      case exc: Exception =>
        makeSnackbar(exc.getMessage).show
        exc.printStackTrace()
    }
    true
  }

  protected override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    if (resultCode != Activity.RESULT_OK || requestCode < 0 || requestCode >= adapter.getCount)
      super.onActivityResult(requestCode, resultCode, data)
    else {
      val uri = data.getExtras.get(Intent.EXTRA_SHORTCUT_INTENT).asInstanceOf[Intent].toUri(0)
      val editor = PreferenceManager.getDefaultSharedPreferences(this).edit
      val id = Integer.toString(widgetId)
      if (!InvisibleWidgetManager.getEmptyIntentUri(this).equals(uri))
        editor.putString(id + InvisibleWidgetManager.OPTIONS_URI, uri)
          .putBoolean(id + InvisibleWidgetManager.OPTIONS_DOUBLE, triggerOnDoubleTap)
      else editor.remove(id + InvisibleWidgetManager.OPTIONS_URI).remove(id + InvisibleWidgetManager.OPTIONS_DOUBLE)
      editor.apply()
      InvisibleWidgetManager.update(this, AppWidgetManager.getInstance(this), widgetId)
      setResult(Activity.RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
      finish()
    }
  }
}
