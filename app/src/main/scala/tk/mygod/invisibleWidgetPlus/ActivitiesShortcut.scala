package tk.mygod.invisibleWidgetPlus

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.support.v4.view.ViewCompat
import android.view.{View, ViewGroup}
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ExpandableListView.OnChildClickListener
import android.widget._
import tk.mygod.animation.AnimationHelper
import tk.mygod.app.ToolbarActivity
import tk.mygod.util.UriUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author Mygod
 */
final class ActivitiesShortcut extends ToolbarActivity with OnChildClickListener with OnItemLongClickListener {
  private final class ActivitiesExpandableListAdapter extends BaseExpandableListAdapter {
    ActivitiesFetcher.init(getPackageManager)
    private val packages = ActivitiesFetcher.packages
    val activitiesCounts = ActivitiesFetcher.activitiesCounts

    override def getChildId(groupPosition: Int, childPosition: Int) = activitiesCounts(groupPosition) + childPosition
    override def getChild(groupPosition: Int, childPosition: Int) =
      packages(groupPosition).exportedActivities(childPosition)
    override def getGroupCount = packages.size
    override def isChildSelectable(groupPosition: Int, childPosition: Int) = true
    override def getGroupId(groupPosition: Int) = groupPosition
    override def getGroup(groupPosition: Int) = packages(groupPosition)
    override def getChildrenCount(groupPosition: Int) = packages(groupPosition).exportedActivities.size
    override def hasStableIds = true

    override def getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View, parent: ViewGroup) = {
      val result = if (convertView == null) getLayoutInflater.inflate(R.layout.icon_list_item_2, parent, false)
        else convertView
      val startPadding = (28 * getResources.getDisplayMetrics.density + 0.5).toInt
      val endPadding = (4 * getResources.getDisplayMetrics.density + 0.5).toInt
      ViewCompat.setPaddingRelative(result, startPadding, 0, endPadding, 0)
      val info = packages(groupPosition).packageInfo
      val manager = getPackageManager
      result.findViewById(android.R.id.icon).asInstanceOf[ImageView]
        .setImageDrawable(info.applicationInfo.loadIcon(manager))
      result.findViewById(android.R.id.text1).asInstanceOf[TextView].setText(info.applicationInfo.loadLabel(manager))
      result.findViewById(android.R.id.text2).asInstanceOf[TextView].setText(info.packageName)
      result
    }

    override def getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View,
                              parent: ViewGroup) = {
      val result = if (convertView == null) getLayoutInflater.inflate(R.layout.icon_list_item_2, parent, false)
        else convertView
      result.setBackgroundResource(R.color.background_darker)
      val startPadding = (56 * getResources.getDisplayMetrics.density + 0.5).toInt
      val endPadding = (4 * getResources.getDisplayMetrics.density + 0.5).toInt
      ViewCompat.setPaddingRelative(result, startPadding, 0, endPadding, 0)
      val info = packages(groupPosition).exportedActivities(childPosition)
      val manager = getPackageManager
      result.findViewById(android.R.id.icon).asInstanceOf[ImageView].setImageDrawable(info.loadIcon(manager))
      result.findViewById(android.R.id.text1).asInstanceOf[TextView].setText(info.loadLabel(manager))
      result.findViewById(android.R.id.text2).asInstanceOf[TextView].setText(if (info.name.startsWith(info.packageName))
        info.name.substring(info.packageName.length()) else info.name)
      result
    }
  }

  private var adapter: ActivitiesExpandableListAdapter = null

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.activities_chooser)
    configureToolbar(R.drawable.ic_close)
    val list = findViewById(android.R.id.list).asInstanceOf[ExpandableListView]
    Future {
      adapter = new ActivitiesExpandableListAdapter()
      runOnUiThread {
        list.setAdapter(adapter)
        AnimationHelper.crossFade(ActivitiesShortcut.this, findViewById(android.R.id.empty), list)
      }
    }
    list.setOnChildClickListener(this)
    list.setOnItemLongClickListener(this)
  }

  override def onChildClick(parent: ExpandableListView, v: View, groupPosition: Int, childPosition: Int, id: Long) = {
    val info = adapter.getChild(groupPosition, childPosition)
    try setResult(Activity.RESULT_OK, new Intent()
      .putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent().setClassName(info.packageName, info.name))
      .putExtra(Intent.EXTRA_SHORTCUT_NAME, info.loadLabel(getPackageManager))
      .putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource
      .fromContext(createPackageContext(info.packageName, 0), info.getIconResource)))
    catch {
      case e: PackageManager.NameNotFoundException => e.printStackTrace() // how is this possible though
    }
    finish
    true
  }

  override def onItemLongClick(parent: AdapterView[_], view: View, position: Int, id: Long) = {
    val groupPosition = ExpandableListView.getPackedPositionGroup(id)
    try ExpandableListView.getPackedPositionType(id) match {
      case ExpandableListView.PACKED_POSITION_TYPE_GROUP =>
        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
          .setData("package:" + adapter.getGroup(groupPosition).packageInfo.packageName))
        true
      case ExpandableListView.PACKED_POSITION_TYPE_CHILD =>
        val info = adapter.getChild(groupPosition,
          ExpandableListView.getPackedPositionChild(id) - adapter.activitiesCounts(groupPosition))
        startActivity(new Intent().setClassName(info.packageName, info.name))
        true
      case _ => false
    } catch {
      case exc: Throwable =>
        showToast(exc.getMessage)
        exc.printStackTrace()
        true
    }
  }
}
