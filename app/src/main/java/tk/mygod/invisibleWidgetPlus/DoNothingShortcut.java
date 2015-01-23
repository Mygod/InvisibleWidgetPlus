package tk.mygod.invisibleWidgetPlus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * @author Mygod
 */
public final class DoNothingShortcut extends Activity {
    private static Intent emptyIntent;
    private static String emptyIntentUri;

    static Intent getEmptyIntent(Context context) {
        if (emptyIntent == null) emptyIntent = new Intent(context, DoNothingShortcut.class);
        return emptyIntent;
    }
    static String getEmptyIntentUri(Context context) {
        if (emptyIntentUri == null) emptyIntentUri = getEmptyIntent(context).toUri(0);
        return emptyIntentUri;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) setResult(RESULT_OK, new Intent()
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, getEmptyIntent(this))
                .putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.action_do_nothing))
                .putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                        Intent.ShortcutIconResource.fromContext(this, R.drawable.invisible)));
        finish();
    }
}
