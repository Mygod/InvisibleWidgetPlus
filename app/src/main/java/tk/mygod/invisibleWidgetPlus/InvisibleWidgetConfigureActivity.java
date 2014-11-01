package tk.mygod.invisibleWidgetPlus;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.melnykov.fab.FloatingActionButton;
import tk.mygod.util.CollectionUtils;
import tk.mygod.util.Predicate;

import java.util.*;

public class InvisibleWidgetConfigureActivity extends ActionBarActivity
        implements ExpandableListView.OnChildClickListener, AdapterView.OnItemLongClickListener {
    private static final Predicate<PackageInfo> packagePredicate = new Predicate<PackageInfo>() {
        @Override
        public boolean apply(PackageInfo obj) {
            return obj.activities != null && obj.activities.length > 0;
        }
    };
    private static final Predicate<ActivityInfo> activityPredicate = new Predicate<ActivityInfo>() {
        @Override
        public boolean apply(ActivityInfo obj) {
            return obj.exported;
        }
    };
    private final Comparator<PackageInfo> packageSorter = new Comparator<PackageInfo>() {
        @Override
        public int compare(PackageInfo lhs, PackageInfo rhs) {
            PackageManager manager = getPackageManager();
            String ll = lhs.applicationInfo.loadLabel(manager).toString(),
                   rl = rhs.applicationInfo.loadLabel(manager).toString();
            int result = ll.toLowerCase().compareTo(rl.toLowerCase());
            return result == 0 ? ll.compareTo(rl) : result;
        }
    };

    private class ActivitiesExpandableListAdapter extends BaseExpandableListAdapter {
        public ActivitiesExpandableListAdapter() {
            Collections.sort(packages, packageSorter);
            activitiesCounts = new int[packages.size() + 1];
            activitiesCounts[0] = 0;
            int i = 1, j = 0;
            for (PackageInfo info : packages) {
                ArrayList<ActivityInfo> list = CollectionUtils.filterArray(info.activities, activityPredicate);
                activities.put(info, list);
                activitiesCounts[i] = activitiesCounts[j] + list.size();
                j = i++;
            }
        }

        private List<PackageInfo> packages = CollectionUtils.filter(getPackageManager()
                .getInstalledPackages(PackageManager.GET_ACTIVITIES), packagePredicate);
        private HashMap<PackageInfo, ArrayList<ActivityInfo>>
                activities = new HashMap<PackageInfo, ArrayList<ActivityInfo>>();
        private int[] activitiesCounts;

        @Override
        public int getGroupCount() {
            return packages.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return activities.get(packages.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return packages.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return activities.get(packages.get(groupPosition)).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return activitiesCounts[groupPosition] + childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.icon_list_item_2, parent, false);
            PackageInfo info = packages.get(groupPosition);
            PackageManager manager = getPackageManager();
            ((ImageView) convertView.findViewById(android.R.id.icon))
                    .setImageDrawable(info.applicationInfo.loadIcon(manager));
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(info.applicationInfo.loadLabel(manager));
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(info.packageName);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                                 ViewGroup parent) {
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.icon_list_item_2, parent, false);
            convertView.setBackgroundResource(R.color.background_darker);
            int padding = (int) (56 * getResources().getDisplayMetrics().density + 0.5);
            if (Build.VERSION.SDK_INT >= 17) convertView.setPaddingRelative(padding, 0, 0, 0);
            else convertView.setPadding(padding, 0, 0, 0);
            ActivityInfo info = activities.get(packages.get(groupPosition)).get(childPosition);
            PackageManager manager = getPackageManager();
            ((ImageView) convertView.findViewById(android.R.id.icon)).setImageDrawable(info.loadIcon(manager));
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(info.loadLabel(manager));
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(info.name.startsWith(info.packageName)
                    ? info.name.substring(info.packageName.length()) : info.name);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private ActivitiesExpandableListAdapter adapter;

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
        setContentView(R.layout.activity_chooser);
        final ExpandableListView list = (ExpandableListView) findViewById(android.R.id.list);
        (new Thread() {
            @Override
            public void run() {
                adapter = new ActivitiesExpandableListAdapter();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list.setAdapter(adapter);
                    }
                });
            }
        }).start();
        list.setEmptyView(findViewById(android.R.id.empty));
        list.setOnChildClickListener(this);
        list.setOnItemLongClickListener(this);
        ((FloatingActionButton) findViewById(R.id.fab)).attachToListView(list);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void doNothing(View view) {
        save(PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(Integer.toString(widgetId), ""));
        setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId));
        finish();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        ActivityInfo info = (ActivityInfo) adapter.getChild(groupPosition, childPosition);
        String prefix = Integer.toString(widgetId);
        save(PreferenceManager.getDefaultSharedPreferences(this).edit().putString(prefix, info.packageName)
                         .putString(prefix + "_name", info.name));
        setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId));
        InvisibleWidget.update(this, AppWidgetManager.getInstance(this), widgetId);
        finish();
        return true;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            int groupPosition = ExpandableListView.getPackedPositionGroup(id);
            ActivityInfo info = (ActivityInfo) adapter.getChild(groupPosition,
                    ExpandableListView.getPackedPositionChild(id) - adapter.activitiesCounts[groupPosition]);
            startActivity(new Intent().setClassName(info.packageName, info.name));
            return true;
        }
        return false;
    }

    private void save(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT >= 9) editor.apply();
        else editor.commit();
    }
}
