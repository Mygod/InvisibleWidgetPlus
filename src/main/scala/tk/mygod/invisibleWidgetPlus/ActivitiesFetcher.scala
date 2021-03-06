package tk.mygod.invisibleWidgetPlus

import android.content.Context
import android.content.pm.PackageManager

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * Reusing cached data.
 *
 * @author Mygod
 */
private object ActivitiesFetcher {
  var packages: mutable.Buffer[Package] = _
  var activitiesCounts: Array[Int] = _

  def init(context: Context) = synchronized(if (packages == null) {
    val manager = context.getPackageManager
    packages = manager.getInstalledPackages(PackageManager.GET_ACTIVITIES).map(new Package(_))
      .filter(p => p.packageInfo.applicationInfo.enabled && p.exportedActivities != null &&
        p.exportedActivities.length > 0)
      .sortWith((lhs, rhs) => InvisibleWidgetManager.lessThanCaseInsensitive(
        lhs.packageInfo.applicationInfo.loadLabel(manager).toString, lhs.packageInfo.packageName,
        rhs.packageInfo.applicationInfo.loadLabel(manager).toString, rhs.packageInfo.packageName))
    activitiesCounts = new Array[Int](packages.size + 1)
    var i = 1
    var j = 0
    for (p <- packages) {
      activitiesCounts(i) = activitiesCounts(j) + p.exportedActivities.length
      j = i
      i += 1
    }
    PackageListener.init(context)
  })
}
