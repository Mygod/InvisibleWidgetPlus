package tk.mygod.invisibleWidgetPlus;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
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
            return shortcuts.size();
        }

        @Override
        public Object getItem(int position) {
            return shortcuts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.icon_list_item_1, parent, false);
            ViewCompat.setPaddingRelative(convertView, 0, 0, 0, 0);
            PackageManager manager = getPackageManager();
            ResolveInfo info = shortcuts.get(position);
            ((ImageView) convertView.findViewById(android.R.id.icon)).setImageDrawable(info.loadIcon(manager));
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(info.loadLabel(manager));
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
        ActivityInfo info = ((ResolveInfo) adapter.getItem(position)).activityInfo;
        startActivityForResult(new Intent(Intent.ACTION_CREATE_SHORTCUT).setClassName(info.packageName, info.name)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId), position);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || requestCode < 0 || requestCode >= adapter.getCount()) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        String uri = ((Intent) data.getExtras().get(Intent.EXTRA_SHORTCUT_INTENT)).toUri(0);
        if (!DoNothingShortcut.getEmptyIntentUri(this).equals(uri)) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putString(Integer.toString(widgetId), uri);
            editor.apply();
            InvisibleWidget.update(this, AppWidgetManager.getInstance(this), widgetId);
        }
        setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId));
        finish();
    }
}
