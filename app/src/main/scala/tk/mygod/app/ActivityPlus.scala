package tk.mygod.app

import android.app.Activity
import tk.mygod.content.ContextPlus

/**
 * @author Mygod
 */
trait ActivityPlus extends Activity with ContextPlus {
  def runOnUiThread(f: => Unit): Unit = runOnUiThread(new Runnable() { def run() = f })
}
