package adrienmalin.pingpoints

import android.arch.lifecycle.ViewModel

class VictoryModel : ViewModel() {
    var matchFinished: Boolean = false
    var winnerName:String = ""
    var player1Name = ""
    var player2Name = ""
    var score = ""
    var previousMatches: String = ""
}