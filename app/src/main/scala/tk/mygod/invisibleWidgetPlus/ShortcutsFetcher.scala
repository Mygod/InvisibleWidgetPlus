package tk.mygod.invisibleWidgetPlus

import android.content.Intent
import android.content.pm.{ResolveInfo, PackageManager}
import tk.mygod.text.TextUtils

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * @author Mygod
 */
private object ShortcutsFetcher {
  private var shortcuts: mutable.Buffer[ResolveInfo] = null

  def getShortcuts(manager: PackageManager) = {
    synchronized {
      if (shortcuts == null) shortcuts = manager.queryIntentActivities(new Intent(Intent.ACTION_CREATE_SHORTCUT), 0)
        .sortWith((lhs, rhs) => TextUtils.lessThanCaseInsensitive(lhs.loadLabel(manager).toString,
                                                                  rhs.loadLabel(manager).toString))
    }
    shortcuts
  }
}
