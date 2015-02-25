package tk.mygod.invisibleWidgetPlus

import android.app.Activity
import android.content.Intent
import android.content.pm.{PackageInfo, PackageManager}
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.widget.Toolbar
import android.view.{View, ViewGroup}
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ExpandableListView.OnChildClickListener
import android.widget._
import tk.mygod.animation.AnimationHelper
import tk.mygod.app.ActivityPlus
import tk.mygod.support.v7.util.ToolbarConfigurer
import tk.mygod.text.TextUtils

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author Mygod
 */
final class ActivitiesShortcut extends ActivityPlus with OnChildClickListener with OnItemLongClickListener {
  private final class Package(val packageInfo : PackageInfo) {
    val exportedActivities = if (packageInfo.activities == null) null
      else packageInfo.activities.filter(info => info.exported)
  }

  private final class ActivitiesExpandableListAdapter extends BaseExpandableListAdapter {
    private val packages = getPackageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES).map(new Package(_))
      .filter(p => p.exportedActivities != null && p.exportedActivities.length > 0).sortWith((lhs, rhs) => {
        val manager = getPackageManager
        TextUtils.lessThanCaseInsensitive(lhs.packageInfo.applicationInfo.loadLabel(manager).toString,
                                          rhs.packageInfo.applicationInfo.loadLabel(manager).toString)
      })
    val activitiesCounts = new Array[Int](packages.size + 1);
    {
      var i = 1
      var j = 0
      for (p <- packages) {
        activitiesCounts(i) = activitiesCounts(j) + p.exportedActivities.size
        j = i
        i += 1
      }
    }

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
      val padding = (56 * getResources.getDisplayMetrics.density + 0.5).toInt
      ViewCompat.setPaddingRelative(result, padding, 0, 0, 0)
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
    new ToolbarConfigurer(this, findViewById(R.id.toolbar).asInstanceOf[Toolbar], R.drawable.ic_close)
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
    if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
      val groupPosition = ExpandableListView.getPackedPositionGroup(id)
      val info = adapter.getChild(groupPosition,
        ExpandableListView.getPackedPositionChild(id) - adapter.activitiesCounts(groupPosition))
      startActivity(new Intent().setClassName(info.packageName, info.name))
      true
    }
    false
  }
}