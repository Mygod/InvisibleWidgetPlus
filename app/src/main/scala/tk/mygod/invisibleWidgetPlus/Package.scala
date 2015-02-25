package tk.mygod.invisibleWidgetPlus

import android.content.pm.PackageInfo

/**
 * @author Mygod
 */
private final class Package(val packageInfo : PackageInfo) {
  val exportedActivities = if (packageInfo.activities == null) null
  else packageInfo.activities.filter(info => info.exported && info.enabled)
}
