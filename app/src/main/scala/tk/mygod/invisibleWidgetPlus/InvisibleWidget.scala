package tk.mygod.invisibleWidgetPlus

import android.appwidget.{AppWidgetManager, AppWidgetProvider}
import android.content.Context
import android.preference.PreferenceManager

/**
 * @author Mygod
 */
final class InvisibleWidget extends AppWidgetProvider {
  override def onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: Array[Int]) =
    for (appWidgetId <- appWidgetIds) InvisibleWidgetManager.update(context, appWidgetManager, appWidgetId)

  override def onDeleted(context: Context, appWidgetIds: Array[Int]) = for (appWidgetId <- appWidgetIds)
    PreferenceManager.getDefaultSharedPreferences(context).edit.remove(appWidgetId.toString).apply()
}
