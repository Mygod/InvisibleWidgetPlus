package tk.mygod.text

/**
 * @author Mygod
 */
object TextUtils {
  def lessThanCaseInsensitive(lhs: String, rhs: String) = {
    val result = lhs.compareToIgnoreCase(rhs)
    if (result == 0) lhs < rhs else result < 0
  }
}
