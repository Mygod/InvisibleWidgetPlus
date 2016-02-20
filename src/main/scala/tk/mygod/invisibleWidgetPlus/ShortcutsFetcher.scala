package tk.mygod.invisibleWidgetPlus

import android.content.pm.ResolveInfo
import android.content.{Context, Intent}

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * @author Mygod
 */
private object ShortcutsFetcher {
  var shortcuts: mutable.Buffer[ResolveInfo] = null

  def getShortcuts(context: Context) = {
    synchronized {
      val manager = context.getPackageManager
      if (shortcuts == null) shortcuts = manager.queryIntentActivities(new Intent(Intent.ACTION_CREATE_SHORTCUT), 0)
        .sortWith((lhs, rhs) => InvisibleWidgetManager.lessThanCaseInsensitive(
          lhs.loadLabel(manager).toString, lhs.activityInfo.name,
          rhs.loadLabel(manager).toString, rhs.activityInfo.name))
      PackageListener.init(context)
    }
    shortcuts
  }
}
