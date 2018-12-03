package adrienmalin.pingpoints

import android.os.Build
import android.text.Html


fun Html.fromHtml2(source: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(source, Html.FROM_HTML_MODE_COMPACT)
    } else {
        Html.fromHtml(source)
    }
}