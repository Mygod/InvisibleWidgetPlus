package tk.mygod.invisibleWidgetPlus;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link InvisibleWidgetConfigureActivity InvisibleWidgetConfigureActivity}
 */
public class InvisibleWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; ++i) update(context, appWidgetManager, appWidgetIds[i]);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; ++i) {
            String prefix = Integer.toString(appWidgetIds[i]);
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove(prefix).remove(prefix + "_name")
                             .apply();
        }
    }

    public static void update(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.invisible_widget);
        String prefix = Integer.toString(appWidgetId), packageName = pref.getString(prefix, "");
        if (packageName != "") views.setOnClickPendingIntent(R.id.button, PendingIntent.getActivity
                (context, 0, new Intent().setClassName(packageName, pref.getString(prefix + "_name", "")), 0));
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}


