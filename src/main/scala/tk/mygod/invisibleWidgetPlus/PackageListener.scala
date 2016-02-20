package tk.mygod.invisibleWidgetPlus

import android.content.{Context, Intent, IntentFilter}

/**
  * @author Mygod
  */
object PackageListener {
  private var registered : Boolean = _

  def init(context: Context) = synchronized(if (!registered) {
    val filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED)
    filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
    filter.addDataScheme("package")
    context.registerReceiver((context: Context, intent: Intent) =>
      if (intent.getAction != Intent.ACTION_PACKAGE_REMOVED || !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
        ActivitiesFetcher.synchronized(ActivitiesFetcher.packages = null)
        ShortcutsFetcher.synchronized(ShortcutsFetcher.shortcuts = null)
      }, filter)
    registered = true
  })
}
