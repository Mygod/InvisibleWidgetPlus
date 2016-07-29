package tk.mygod.invisibleWidgetPlus

import java.net.URISyntaxException

import android.app.{Activity, ActivityOptions, PendingIntent}
import android.appwidget.AppWidgetManager
import android.content.{Context, Intent}
import android.view.View
import android.widget.RemoteViews
import tk.mygod.os.Build

import scala.collection.mutable

/**
 * @author Mygod
 */
object InvisibleWidgetManager {
  final val ACTION_TAP = "tk.mygod.invisibleWidgetPlus.InvisibleWidgetManager.ACTION_TAP"
  final val EXTRA_URI = "tk.mygod.invisibleWidgetPlus.InvisibleWidgetManager.EXTRA_URI"

  final val OPTIONS_URI = "uri"
  final val OPTIONS_DOUBLE = "double"

  private var emptyIntent: Intent = _
  private var emptyIntentUri: String = _
  def getEmptyIntent(context: Context) = {
    if (emptyIntent == null) emptyIntent = new Intent(context, classOf[DoNothingShortcut])
    emptyIntent
  }
  def getEmptyIntentUri(context: Context) = {
    if (emptyIntentUri == null) emptyIntentUri = getEmptyIntent(context).toUri(0)
    emptyIntentUri
  }

  def update(context: Context, awm: AppWidgetManager, appWidgetId: Int) = {
    val options = awm.getAppWidgetOptions(appWidgetId)
    val uri = options.getString(OPTIONS_URI, "")
    if (!uri.isEmpty) {
      val views = new RemoteViews(context.getPackageName, R.layout.invisible_widget)
      try views.setOnClickPendingIntent(R.id.button, if (options.getBoolean(OPTIONS_DOUBLE, false))
        PendingIntent.getBroadcast(context, 0, new Intent(context, classOf[InvisibleWidget]).setAction(ACTION_TAP)
          .putExtra(EXTRA_URI, uri), 0)
        else PendingIntent.getActivity(context, 0, Intent.parseUri(uri, 0), 0))
      catch {
        case e: URISyntaxException => e.printStackTrace // seriously though, you really shouldn't reach this point
      }
      awm.updateAppWidget(appWidgetId, views)
    }
  }

  val tapped = new mutable.HashMap[String, Long]
  def tap(context: Context, uri: String) {
    val now = System.currentTimeMillis
    tapped.get(uri) match {
      case Some(last) if now - last < 500 =>
        context.startActivity(Intent.parseUri(uri, 0).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
      case _ =>
    }
    tapped(uri) = now
  }

  def lessThanCaseInsensitive(lhs1: String, lhs2: String, rhs1: String, rhs2: String): Boolean = {
    var result = lhs1.compareToIgnoreCase(rhs1)
    if (result != 0) return result < 0
    result = lhs1.compareTo(rhs1)
    if (result != 0) return result < 0
    result = lhs2.compareToIgnoreCase(rhs2)
    if (result != 0) result < 0 else lhs2 < rhs2
  }

  def makeRevealAnimation(view: View) = (if (Build.version >= 23)
    ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.getMeasuredWidth, view.getMeasuredHeight)
  else ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getMeasuredWidth, view.getMeasuredHeight)).toBundle
  def makeLocalRevealAnimation(activity: Activity, view: View) = (if (Build.version >= 21)
    ActivityOptions.makeSceneTransitionAnimation(activity)
  else ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getMeasuredWidth, view.getMeasuredHeight)).toBundle
}
