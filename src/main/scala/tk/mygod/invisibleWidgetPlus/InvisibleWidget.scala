package tk.mygod.invisibleWidgetPlus

import android.appwidget.{AppWidgetManager, AppWidgetProvider}
import android.content.{Context, Intent}

/**
 * @author Mygod
 */
final class InvisibleWidget extends AppWidgetProvider {
  override def onReceive(context: Context, intent: Intent) = intent.getAction match {
    case InvisibleWidgetManager.ACTION_TAP => InvisibleWidgetManager.tap(context, intent.getDataString)
    case _ => super.onReceive(context, intent)
  }

  override def onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: Array[Int]) =
    for (appWidgetId <- appWidgetIds) InvisibleWidgetManager.update(context, appWidgetManager, appWidgetId)
}
