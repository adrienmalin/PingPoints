package adrienmalin.pingpoints

import android.os.Build
import android.text.Html
import android.text.Spanned


@Suppress("DEPRECATION")
fun fromHtml(source: String): Spanned = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> Html.fromHtml(source, Html.FROM_HTML_MODE_COMPACT)
    else -> Html.fromHtml(source)
}