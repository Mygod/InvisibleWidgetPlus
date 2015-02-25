package tk.mygod.invisibleWidgetPlus

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.Toolbar
import android.view.{View, ViewGroup}
import android.widget.AdapterView.OnItemClickListener
import android.widget._
import tk.mygod.animation.AnimationHelper
import tk.mygod.app.ActivityPlus
import tk.mygod.support.v7.util.ToolbarConfigurer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author Mygod
 */
class ShortcutsChooser extends ActivityPlus with OnItemClickListener {
  lazy val manager = getPackageManager

  private final class ShortcutsListAdapter extends BaseAdapter {
    private val shortcuts = ShortcutsFetcher.getShortcuts(getPackageManager)

    override def getCount = shortcuts.size
    override def getItem(position: Int) = shortcuts(position)
    override def getItemId(position: Int) = position
    override def getView(position: Int, convertView: View, parent: ViewGroup) = {
      val result = if (convertView == null) getLayoutInflater.inflate(R.layout.icon_list_item_2, parent, false)
        else convertView
      result.setPadding(0, 0, 0, 0)
      val manager = getPackageManager
      val info = shortcuts(position)
      result.findViewById(android.R.id.icon).asInstanceOf[ImageView].setImageDrawable(info.loadIcon(manager))
      result.findViewById(android.R.id.text1).asInstanceOf[TextView].setText(info.loadLabel(manager))
      result.findViewById(android.R.id.text2).asInstanceOf[TextView].setText(info.activityInfo.name)
      result
    }
  }

  private var adapter: ShortcutsListAdapter = null
  private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    val extras = getIntent.getExtras
    if (extras != null)
      widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish else {
      setContentView(R.layout.actions_chooser)
      new ToolbarConfigurer(this, findViewById(R.id.toolbar).asInstanceOf[Toolbar], R.drawable.ic_close)
      val list = findViewById(android.R.id.list).asInstanceOf[ListView]
      Future {
        adapter = new ShortcutsListAdapter
        runOnUiThread {
          list.setAdapter(adapter)
          AnimationHelper.crossFade(ShortcutsChooser.this, findViewById(android.R.id.empty), list)
        }
      }
      list.setOnItemClickListener(this)
    }
  }

  override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
    val info = adapter.getItem(position).activityInfo
    startActivityForResult(new Intent(Intent.ACTION_CREATE_SHORTCUT).setClassName(info.packageName, info.name)
      .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId), position)
  }

  protected override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    if (resultCode != Activity.RESULT_OK || requestCode < 0 || requestCode >= adapter.getCount)
      super.onActivityResult(requestCode, resultCode, data)
    else {
      val uri = data.getExtras.get(Intent.EXTRA_SHORTCUT_INTENT).asInstanceOf[Intent].toUri(0)
      if (!InvisibleWidgetManager.getEmptyIntentUri(this).equals(uri)) {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit
        editor.putString(Integer.toString(widgetId), uri)
        editor.apply
        InvisibleWidgetManager.update(this, AppWidgetManager.getInstance(this), widgetId)
      }
      setResult(Activity.RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
      finish
    }
  }
}
