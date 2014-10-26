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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import tk.mygod.util.CollectionUtils;
import tk.mygod.util.Predicate;

import java.util.*;


/**
 * The configuration screen for the {@link InvisibleWidget InvisibleWidget} AppWidget.
 */
public class InvisibleWidgetConfigureActivity extends ActionBarActivity
        implements ExpandableListView.OnChildClickListener, AdapterView.OnItemLongClickListener {
    private static final Predicate<PackageInfo> packagePredicate = new Predicate<PackageInfo>() {
        @Override
        public boolean apply(PackageInfo obj) {
            return obj.activities != null;
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
                ArrayList list = CollectionUtils.filterArray(info.activities, activityPredicate);
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

        /**
         * Gets the number of groups.
         *
         * @return the number of groups
         */
        @Override
        public int getGroupCount() {
            return packages.size();
        }

        /**
         * Gets the number of children in a specified group.
         *
         * @param groupPosition the position of the group for which the children
         *                      count should be returned
         * @return the children count in the specified group
         */
        @Override
        public int getChildrenCount(int groupPosition) {
            return activities.get(packages.get(groupPosition)).size();
        }

        /**
         * Gets the data associated with the given group.
         *
         * @param groupPosition the position of the group
         * @return the data child for the specified group
         */
        @Override
        public Object getGroup(int groupPosition) {
            return packages.get(groupPosition);
        }

        /**
         * Gets the data associated with the given child within the given group.
         *
         * @param groupPosition the position of the group that the child resides in
         * @param childPosition the position of the child with respect to other
         *                      children in the group
         * @return the data of the child
         */
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return activities.get(packages.get(groupPosition)).get(childPosition);
        }

        /**
         * Gets the ID for the group at the given position. This group ID must be
         * unique across groups. The combined ID (see
         * {@link #getCombinedGroupId(long)}) must be unique across ALL items
         * (groups and all children).
         *
         * @param groupPosition the position of the group for which the ID is wanted
         * @return the ID associated with the group
         */
        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        /**
         * Gets the ID for the given child within the given group. This ID must be
         * unique across all children within the group. The combined ID (see
         * {@link #getCombinedChildId(long, long)}) must be unique across ALL items
         * (groups and all children).
         *
         * @param groupPosition the position of the group that contains the child
         * @param childPosition the position of the child within the group for which
         *                      the ID is wanted
         * @return the ID associated with the child
         */
        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return activitiesCounts[groupPosition] + childPosition;
        }

        /**
         * Indicates whether the child and group IDs are stable across changes to the
         * underlying data.
         *
         * @return whether or not the same ID always refers to the same object
         */
        @Override
        public boolean hasStableIds() {
            return true;
        }

        /**
         * Gets a View that displays the given group. This View is only for the
         * group--the Views for the group's children will be fetched using
         * {@link #getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)}.
         *
         * @param groupPosition the position of the group for which the View is
         *                      returned
         * @param isExpanded    whether the group is expanded or collapsed
         * @param convertView   the old view to reuse, if possible. You should check
         *                      that this view is non-null and of an appropriate type before
         *                      using. If it is not possible to convert this view to display
         *                      the correct data, this method can create a new view. It is not
         *                      guaranteed that the convertView will have been previously
         *                      created by
         *                      {@link #getGroupView(int, boolean, android.view.View, android.view.ViewGroup)}.
         * @param parent        the parent that this view will eventually be attached to
         * @return the View corresponding to the group at the specified position
         */
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

        /**
         * Gets a View that displays the data for the given child within the given
         * group.
         *
         * @param groupPosition the position of the group that contains the child
         * @param childPosition the position of the child (for which the View is
         *                      returned) within the group
         * @param isLastChild   Whether the child is the last child within the group
         * @param convertView   the old view to reuse, if possible. You should check
         *                      that this view is non-null and of an appropriate type before
         *                      using. If it is not possible to convert this view to display
         *                      the correct data, this method can create a new view. It is not
         *                      guaranteed that the convertView will have been previously
         *                      created by
         *                      {@link #getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)}.
         * @param parent        the parent that this view will eventually be attached to
         * @return the View corresponding to the child at the specified position
         */
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                                 ViewGroup parent) {
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.icon_list_item_2, parent, false);
            ActivityInfo info = activities.get(packages.get(groupPosition)).get(childPosition);
            PackageManager manager = getPackageManager();
            ((ImageView) convertView.findViewById(android.R.id.icon)).setImageDrawable(info.loadIcon(manager));
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(info.loadLabel(manager));
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(info.name.startsWith(info.packageName)
                    ? info.name.substring(info.packageName.length()) : info.name);
            return convertView;
        }

        /**
         * Whether the child at the specified position is selectable.
         *
         * @param groupPosition the position of the group that contains the child
         * @param childPosition the position of the child within the group
         * @return whether the child is selectable.
         */
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
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.configure_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.do_nothing:
                save(PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putString(Integer.toString(widgetId), ""));
                setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId));
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback method to be invoked when a child in this expandable list has
     * been clicked.
     *
     * @param parent The ExpandableListView where the click happened
     * @param v The view within the expandable list/ListView that was clicked
     * @param groupPosition The group position that contains the child that
     *        was clicked
     * @param childPosition The child position within the group
     * @param id The row id of the child that was clicked
     * @return True if the click was handled
     */
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

    /**
     * Callback method to be invoked when an item in this view has been
     * clicked and held.
     * <p/>
     * Implementers can call getItemAtPosition(position) if they need to access
     * the data associated with the selected item.
     *
     * @param parent   The AbsListView where the click happened
     * @param view     The view within the AbsListView that was clicked
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     * @return true if the callback consumed the long click, false otherwise
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            ActivityInfo info = (ActivityInfo) adapter.getChild(ExpandableListView.getPackedPositionGroup(id),
                                                                ExpandableListView.getPackedPositionChild(id));
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



