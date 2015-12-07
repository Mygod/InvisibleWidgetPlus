package tk.mygod.invisibleWidgetPlus

import android.content.{Intent, Context, BroadcastReceiver}

/**
  * @author Mygod
  */
class PackageListener extends BroadcastReceiver {
  override def onReceive(context: Context, intent: Intent) =
    if (intent.getAction != Intent.ACTION_PACKAGE_REMOVED || !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
      ActivitiesFetcher.synchronized(ActivitiesFetcher.packages = null)
      ShortcutsFetcher.synchronized(ShortcutsFetcher.shortcuts = null)
    }
}
