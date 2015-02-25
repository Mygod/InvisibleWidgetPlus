package tk.mygod.net

import android.net.Uri

/**
 * @author Mygod
 */
object Utils {
  implicit def parseUri(uriString: String): Uri = Uri.parse(uriString)
}
