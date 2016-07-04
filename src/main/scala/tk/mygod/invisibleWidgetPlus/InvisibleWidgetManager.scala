package tk.mygod.invisibleWidgetPlus

import java.net.URISyntaxException

import android.app.{ActivityOptions, PendingIntent}
import android.appwidget.AppWidgetManager
import android.content.{Context, Intent}
import android.os.Handler
import android.preference.PreferenceManager
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

  def update(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) = {
    val pref = PreferenceManager.getDefaultSharedPreferences(context)
    val id = appWidgetId.toString
    val uri = pref.getString(id, "")
    if (!uri.isEmpty) {
      val views = new RemoteViews(context.getPackageName, R.layout.invisible_widget)
      try views.setOnClickPendingIntent(R.id.button, if (pref.getBoolean(id + "_double", false))
        PendingIntent.getBroadcast(context, 0,
          new Intent(context, classOf[InvisibleWidget]).setAction(ACTION_TAP).putExtra(EXTRA_ID, appWidgetId), 0)
        else PendingIntent.getActivity(context, 0, Intent.parseUri(uri, 0), 0))
      catch {
        case e: URISyntaxException => e.printStackTrace // seriously though, you really shouldn't reach this point
      }
      appWidgetManager.updateAppWidget(appWidgetId, views)
    }
  }

  lazy val handler = new Handler
  val tapped = new mutable.HashSet[Int]
  def tap(context: Context, id: Int) = if (tapped.add(id)) handler.postDelayed(() => tapped.remove(id), 500)
    else context.startActivity(Intent.parseUri(PreferenceManager.getDefaultSharedPreferences(context)
      .getString(id.toString, ""), 0).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

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
}
