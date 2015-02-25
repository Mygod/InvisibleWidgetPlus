package tk.mygod.invisibleWidgetPlus

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/**
 * @author Mygod
 */
final class DoNothingShortcut extends Activity {
  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent.getAction)) setResult(Activity.RESULT_OK, new Intent()
      .putExtra(Intent.EXTRA_SHORTCUT_INTENT, InvisibleWidgetManager.getEmptyIntent(this))
      .putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.action_do_nothing))
      .putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
        Intent.ShortcutIconResource.fromContext(this, R.drawable.invisible)))
    finish
  }
}
