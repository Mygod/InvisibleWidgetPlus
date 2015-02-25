package tk.mygod.app

import android.app.Activity

/**
 * @author Mygod
 */
trait ActivityPlus extends Activity {
  def runOnUiThread(f: => Unit): Unit = runOnUiThread(new Runnable() { def run() = f })
}
