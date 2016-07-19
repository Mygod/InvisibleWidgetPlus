package tk.mygod.invisibleWidgetPlus

import java.net.URISyntaxException

import android.app.{Activity, ActivityOptions, PendingIntent}
import android.appwidget.AppWidgetManager
import android.content.{Context, Intent}
import android.os.Handler
import android.view.View
import android.widget.RemoteViews
import tk.mygod.os.Build

import scala.collection.mutable

/**
 * @author Mygod
 */
object InvisibleWidgetManager {
  final val ACTION_TAP = "tk.mygod.invisibleWidgetPlus.InvisibleWidgetManager.ACTION_TAP"
  final val EXTRA_ID = "tk.mygod.invisibleWidgetPlus.InvisibleWidgetManager.EXTRA_ID"
  final val EXTRA_URI = "tk.mygod.invisibleWidgetPlus.InvisibleWidgetManager.EXTRA_URI"

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
    val uri = options.getString("uri", "")
    if (!uri.isEmpty) {
      val views = new RemoteViews(context.getPackageName, R.layout.invisible_widget)
      try views.setOnClickPendingIntent(R.id.button, if (options.getBoolean("double", false))
        PendingIntent.getBroadcast(context, 0, new Intent(context, classOf[InvisibleWidget]).setAction(ACTION_TAP)
          .putExtra(EXTRA_ID, appWidgetId).putExtra(EXTRA_URI, options.getString("uri")), 0)
        else PendingIntent.getActivity(context, 0, Intent.parseUri(uri, 0), 0))
      catch {
        case e: URISyntaxException => e.printStackTrace // seriously though, you really shouldn't reach this point
      }
      awm.updateAppWidget(appWidgetId, views)
    }
  }

  lazy val handler = new Handler
  val tapped = new mutable.HashSet[Int]
  def tap(context: Context, id: Int, uri: String) = if (tapped.add(id)) handler.postDelayed(() => tapped.remove(id), 500)
    else context.startActivity(Intent.parseUri(uri, 0).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

  def lessThanCaseInsensitive(lhs1: String, lhs2: String, rhs1: String, rhs2: String): Boolean = {
    var result = lhs1.compareToIgnoreCase(rhs1)
    if (result != 0) return result < 0
    result = lhs1.compareTo(rhs1)
    if (result != 0) return result < 0
    result = lhs2.compareToIgnoreCase(rhs2)
    if (result != 0) result < 0 else lhs2 < rhs2
  }

  def makeRevealAnimation(activity: Activity, view: View) = (if (Build.version >= 21)
    ActivityOptions.makeSceneTransitionAnimation(activity)  // todo: detect?
  else ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getMeasuredWidth, view.getMeasuredHeight)).toBundle
}
