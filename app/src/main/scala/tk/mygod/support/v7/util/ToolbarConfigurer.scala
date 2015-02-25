package tk.mygod.support.v7.util

import android.app.Activity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.View.OnClickListener
import tk.mygod.invisibleWidgetPlus.R

/**
 * @author Mygod
 */
final class ToolbarConfigurer(private val activity: Activity, toolbar: Toolbar, navigationIcon: Int = 0)
  extends OnClickListener {
  toolbar.setTitle(activity.getTitle)
  if (navigationIcon != -1) {
    toolbar.setNavigationIcon(if (navigationIcon == 0) R.drawable.abc_ic_ab_back_mtrl_am_alpha else navigationIcon)
    toolbar.setNavigationOnClickListener(this)
  }

  override def onClick(v: View) = {
    val intent = activity.getParentActivityIntent
    if (intent == null) activity.finish else activity.navigateUpTo(intent)
  }
}
