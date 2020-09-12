package adrienmalin.pingpoints

import androidx.lifecycle.ViewModel

class VictoryModel : ViewModel() {
    var matchFinished: Boolean = false
    var winnerName:String = ""
    var players: List<Player> = emptyList()
    var previousMatches: String = ""
}