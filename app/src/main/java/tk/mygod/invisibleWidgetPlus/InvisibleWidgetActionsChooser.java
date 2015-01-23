package tk.mygod.invisibleWidgetPlus;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import tk.mygod.support.v7.util.ToolbarConfigurer;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InvisibleWidgetActionsChooser extends Activity implements ListView.OnItemClickListener {
    private final Comparator<ResolveInfo> shortcutSorter = new Comparator<ResolveInfo>() {
        @Override
        public int compare(ResolveInfo lhs, ResolveInfo rhs) {
            PackageManager manager = getPackageManager();
            String ll = lhs.loadLabel(manager).toString(), rl = rhs.loadLabel(manager).toString();
            int result = ll.toLowerCase().compareTo(rl.toLowerCase());
            return result == 0 ? ll.compareTo(rl) : result;
        }
    };

    private class ActionsListAdapter extends BaseAdapter {
        public ActionsListAdapter() {
            Collections.sort(shortcuts, shortcutSorter);
        }

        private List<ResolveInfo> shortcuts = getPackageManager()
                .queryIntentActivities(new Intent(Intent.ACTION_CREATE_SHORTCUT), 0);

        @Override
        public int getCount() {
            return shortcuts.size() + 2;
        }

        @Override
        public Object getItem(int position) {
            return position == 0 ? null : position == 1 ? "activities" : shortcuts.get(position - 2);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.icon_list_item_1, parent, false);
            if (Build.VERSION.SDK_INT >= 17) convertView.setPaddingRelative(0, 0, 0, 0);
            else convertView.setPadding(0, 0, 0, 0);
            PackageManager manager = getPackageManager();
            ImageView icon = (ImageView) convertView.findViewById(android.R.id.icon);
            TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
            if (position > 1) {
                ResolveInfo info = shortcuts.get(position - 2);
                icon.setImageDrawable(info.loadIcon(manager));
                text1.setText(info.loadLabel(manager));
            } else {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.invisible));
                text1.setText(getString(position == 0 ? R.string.action_do_nothing : R.string.action_activities));
            }
            return convertView;
        }
    }

    private ActionsListAdapter adapter;
    private int widgetId;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setResult(RESULT_CANCELED);
        Bundle extras = getIntent().getExtras();
        if (extras != null)
            widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
        setContentView(R.layout.actions_chooser);
        new ToolbarConfigurer(this, (Toolbar) findViewById(R.id.toolbar), R.drawable.ic_close);
        final ListView list = (ListView) findViewById(android.R.id.list);
        (new Thread() {
            @Override
            public void run() {
                adapter = new ActionsListAdapter();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list.setAdapter(adapter);
                        InvisibleWidget.crossFade(InvisibleWidgetActionsChooser.this,
                                findViewById(android.R.id.empty), list);
                    }
                });
            }
        }).start();
        list.setOnItemClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Integer.toString(widgetId), "")
                             .apply();
            setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId));
            finish();
        } else if (position == 1) startActivityForResult(new Intent(this, InvisibleWidgetActivitiesChooser.class), 1);
        else {
            ActivityInfo info = ((ResolveInfo) adapter.getItem(position)).activityInfo;
            startActivityForResult(new Intent(Intent.ACTION_CREATE_SHORTCUT).setClassName(info.packageName, info.name)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId), position);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || requestCode <= 0 || requestCode > adapter.getCount() + 1) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        String prefix = Integer.toString(widgetId);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        if (requestCode == 1) editor.putString(prefix, "activity")
                .putString(prefix + "_package", data.getStringExtra("package"))
                .putString(prefix + "_name", data.getStringExtra("name"));
        else editor.putString(prefix, "shortcut")
                .putString(prefix + "_uri", ((Intent) data.getExtras().get(Intent.EXTRA_SHORTCUT_INTENT)).toUri(0));
        editor.apply();
        InvisibleWidget.update(this, AppWidgetManager.getInstance(this), widgetId);
        setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId));
        finish();
    }
}
