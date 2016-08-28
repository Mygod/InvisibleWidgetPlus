package tk.mygod.invisibleWidgetPlus

import android.appwidget.{AppWidgetManager, AppWidgetProvider}
import android.content.{Context, Intent}
import android.preference.PreferenceManager

/**
 * @author Mygod
 */
final class InvisibleWidget extends AppWidgetProvider {
  import InvisibleWidgetManager._

  override def onReceive(context: Context, intent: Intent) = intent.getAction match {
    case InvisibleWidgetManager.ACTION_TAP => InvisibleWidgetManager.tap(context, intent.getDataString)
    case _ => super.onReceive(context, intent)
  }

  override def onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: Array[Int]) =
    for (appWidgetId <- appWidgetIds) InvisibleWidgetManager.update(context, appWidgetManager, appWidgetId)

  override def onDeleted(context: Context, appWidgetIds: Array[Int]) = for (appWidgetId <- appWidgetIds)
    PreferenceManager.getDefaultSharedPreferences(context).edit
      .remove(appWidgetId + OPTIONS_URI).remove(appWidgetId + OPTIONS_DOUBLE).apply()
}
