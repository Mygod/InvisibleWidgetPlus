package tk.mygod.invisibleWidgetPlus

import java.util.concurrent.atomic.AtomicBoolean

import android.content.{Context, Intent, IntentFilter}

/**
  * @author Mygod
  */
object PackageListener {
  private val registered = new AtomicBoolean
  def init(context: Context) = if (registered.compareAndSet(false, true)) {
    val filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED)
    filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
    filter.addDataScheme("package")
    context.registerReceiver((_, intent) =>
      if (intent.getAction != Intent.ACTION_PACKAGE_REMOVED || !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
        ActivitiesFetcher.synchronized(ActivitiesFetcher.packages = null)
        ShortcutsFetcher.synchronized(ShortcutsFetcher.shortcuts = null)
      }, filter)
  }
}
