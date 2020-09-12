package adrienmalin.pingpoints

data class Player(
    val name: String,
    var score: Int,
    var soundex: String = ""
)
