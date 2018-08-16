package adrienmalin.pingpoints

import android.os.Build
import android.text.Html
import android.text.Spanned


data class Player(
        var name: String,
        var score: Int = 0
)


enum class Side(val value:Int) {
    LEFT(0),
    RIGHT(1)
}


data class State(
        val score: List<Int>,
        val service: Side
)

@SuppressWarnings("deprecation")
fun fromHtml(html: String): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(html)

    }
}
