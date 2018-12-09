package adrienmalin.pingpoints

import java.util.regex.Pattern

data class Player (
    var name: String,
    var score: Int,
    var pattern: Pattern? = null
)