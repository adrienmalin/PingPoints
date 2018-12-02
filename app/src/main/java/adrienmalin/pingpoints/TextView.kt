package adrienmalin.pingpoints

import android.os.Build
import android.text.Html
import android.widget.TextView

fun TextView.setHtmlText(htmlText: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        this.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT));
    } else {
        this.setText(Html.fromHtml(htmlText));
    }
}