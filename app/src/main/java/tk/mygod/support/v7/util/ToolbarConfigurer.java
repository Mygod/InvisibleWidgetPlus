package tk.mygod.support.v7.util;

import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.view.View;
import tk.mygod.invisibleWidgetPlus.R;

/**
 * @author Mygod
 */
public final class ToolbarConfigurer implements View.OnClickListener {
    private Activity activity;

    public ToolbarConfigurer(Activity activity, Toolbar toolbar, int navigationIcon) {
        toolbar.setTitle((this.activity = activity).getTitle());
        if (navigationIcon == -1) return;
        toolbar.setNavigationIcon(navigationIcon == 0 ? R.drawable.abc_ic_ab_back_mtrl_am_alpha : navigationIcon);
        toolbar.setNavigationOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            NavUtils.navigateUpFromSameTask(activity);
        } catch (IllegalArgumentException ignore) { // no PARENT_ACTIVITY configured
            activity.finish();
        }
    }
}
