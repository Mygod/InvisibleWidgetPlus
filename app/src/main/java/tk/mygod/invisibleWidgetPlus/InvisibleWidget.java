package tk.mygod.invisibleWidgetPlus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

import java.net.URISyntaxException;

public class InvisibleWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) update(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            String prefix = Integer.toString(appWidgetId);
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove(prefix).remove(prefix + "_package")
                             .remove(prefix + "_name").remove(prefix + "_uri").apply();
        }
    }

    public static void update(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.invisible_widget);
        String prefix = Integer.toString(appWidgetId), type = pref.getString(prefix, "");
        if (type.equals("activity")) views.setOnClickPendingIntent(R.id.button,
                PendingIntent.getActivity(context, 0, new Intent().setClassName(pref.getString(prefix + "_package", ""),
                        pref.getString(prefix + "_name", "")), 0));
        else if (type.equals("shortcut")) try {
            views.setOnClickPendingIntent(R.id.button,
                    PendingIntent.getActivity(context, 0, Intent.parseUri(pref.getString(prefix + "_uri", ""), 0), 0));
        } catch (URISyntaxException e) {
            e.printStackTrace();    // seriously though, you really shouldn't reach this point
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static void crossFade(Context context, final View from, final View to) {
        int shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
        to.setAlpha(0);
        to.setVisibility(View.VISIBLE);
        to.animate().alpha(1).setDuration(shortAnimTime);
        from.animate().alpha(0).setDuration(shortAnimTime).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                from.setVisibility(View.GONE);
            }
        });
    }
}


