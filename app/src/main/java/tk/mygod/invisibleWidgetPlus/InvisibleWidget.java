package tk.mygod.invisibleWidgetPlus;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class InvisibleWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) update(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            String prefix = Integer.toString(appWidgetId);
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove(prefix).remove(prefix + "_name")
                             .apply();
        }
    }

    public static void update(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.invisible_widget);
        String prefix = Integer.toString(appWidgetId), packageName = pref.getString(prefix, "");
        if (!packageName.equals("")) views.setOnClickPendingIntent(R.id.button, PendingIntent.getActivity
                (context, 0, new Intent().setClassName(packageName, pref.getString(prefix + "_name", "")), 0));
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}


