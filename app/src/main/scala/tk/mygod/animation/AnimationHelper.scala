package tk.mygod.animation

import android.animation.{Animator, AnimatorListenerAdapter}
import android.content.Context
import android.view.View

/**
 * @author Mygod
 */
object AnimationHelper {
  def crossFade(context: Context, from: View, to: View) {
    def shortAnimTime = context.getResources.getInteger(android.R.integer.config_shortAnimTime)
    to.setAlpha(0)
    to.setVisibility(View.VISIBLE)
    to.animate().alpha(1).setDuration(shortAnimTime)
    from.animate().alpha(0).setDuration(shortAnimTime).setListener(new AnimatorListenerAdapter {
      override def onAnimationEnd(animation: Animator) = from.setVisibility(View.GONE)
    })
  }
}
