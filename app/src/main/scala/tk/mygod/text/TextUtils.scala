package tk.mygod.text

/**
 * @author Mygod
 */
object TextUtils {
  def lessThanCaseInsensitive(lhs1: String, lhs2: String, rhs1: String, rhs2: String): Boolean = {
    var result = lhs1.compareToIgnoreCase(rhs1)
    if (result != 0) return result < 0
    result = lhs1.compareTo(rhs1)
    if (result != 0) return result < 0
    result = lhs2.compareToIgnoreCase(rhs2)
    if (result != 0) result < 0 else lhs2 < rhs2
  }
}
