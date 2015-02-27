package tk.mygod.invisibleWidgetPlus

import java.net.URISyntaxException

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.{Intent, Context}
import android.preference.PreferenceManager
import android.widget.RemoteViews

/**
 * @author Mygod
 */
object InvisibleWidgetManager {
  private var emptyIntent: Intent = null
  private var emptyIntentUri: String = null
  def getEmptyIntent(context: Context) = {
    if (emptyIntent == null) emptyIntent = new Intent(context, classOf[DoNothingShortcut])
    emptyIntent
  }
  def getEmptyIntentUri(context: Context) = {
    if (emptyIntentUri == null) emptyIntentUri = getEmptyIntent(context).toUri(0)
    emptyIntentUri
  }

  def update(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) = {
    val uri = PreferenceManager.getDefaultSharedPreferences(context).getString(appWidgetId.toString, "")
    if (!uri.isEmpty) {
      val views = new RemoteViews(context.getPackageName, R.layout.invisible_widget)
      try views.setOnClickPendingIntent(R.id.button, PendingIntent.getActivity(context, 0, Intent.parseUri(uri, 0), 0))
      catch {
        case e: URISyntaxException => e.printStackTrace // seriously though, you really shouldn't reach this point
      }
      appWidgetManager.updateAppWidget(appWidgetId, views)
    }
  }

  def lessThanCaseInsensitive(lhs1: String, lhs2: String, rhs1: String, rhs2: String): Boolean = {
    var result = lhs1.compareToIgnoreCase(rhs1)
    if (result != 0) return result < 0
    result = lhs1.compareTo(rhs1)
    if (result != 0) return result < 0
    result = lhs2.compareToIgnoreCase(rhs2)
    if (result != 0) result < 0 else lhs2 < rhs2
  }
}
