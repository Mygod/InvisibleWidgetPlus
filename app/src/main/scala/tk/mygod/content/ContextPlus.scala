package tk.mygod.content

import android.content.Context
import android.widget.Toast

/**
 * @author Mygod
 */
trait ContextPlus extends Context {
  def showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, text, duration).show
}
