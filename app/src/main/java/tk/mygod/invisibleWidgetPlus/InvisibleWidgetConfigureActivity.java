package tk.mygod.invisibleWidgetPlus;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import tk.mygod.util.CollectionUtils;
import tk.mygod.util.Predicate;
import tk.mygod.widget.RecyclerOnItemClickListener;

import java.util.*;


/**
 * The configuration screen for the {@link InvisibleWidget InvisibleWidget} AppWidget.
 */
public class InvisibleWidgetConfigureActivity extends ActionBarActivity {
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

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView text1, text2;

        public ItemViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(android.R.id.icon);
            text1 = (TextView) itemView.findViewById(android.R.id.text1);
            text2 = (TextView) itemView.findViewById(android.R.id.text2);
        }
    }

    private class ActivitiesAdapter extends RecyclerView.Adapter<ItemViewHolder> {
        public ActivitiesAdapter() {
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

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new ItemViewHolder(getLayoutInflater().inflate(R.layout.icon_list_item_2, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(ItemViewHolder vh, int i) {
            PackageInfo info = packages.get(i);
            PackageManager manager = getPackageManager();
            vh.icon.setImageDrawable(info.applicationInfo.loadIcon(manager));
            vh.text1.setText(info.applicationInfo.loadLabel(manager));
            vh.text2.setText(info.packageName);
        }

        @Override
        public int getItemCount() {
            return packages.size();
        }
    }

    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private ActivitiesAdapter adapter;

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
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        final RecyclerView rv = (RecyclerView) findViewById(android.R.id.list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.addOnItemTouchListener(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView rv, View view) {
                // todo: do something
            }
        });
        final View empty = findViewById(android.R.id.empty);
        (new Thread() {
            @Override
            public void run() {
                adapter = new ActivitiesAdapter();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rv.setVisibility(View.VISIBLE);
                        empty.setVisibility(View.GONE);
                        rv.setAdapter(adapter);
                    }
                });
            }
        }).start();
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
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Integer.toString(widgetId), "")
                                 .apply();
                setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId));
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}



