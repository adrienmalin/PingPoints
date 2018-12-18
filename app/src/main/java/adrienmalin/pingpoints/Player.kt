package adrienmalin.pingpoints

data class Player (
    val name: String,
    var score: Int,
    val soundex: String = soundex(name)
)
