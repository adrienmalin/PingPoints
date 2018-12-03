package adrienmalin.pingpoints

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel


class MatchModel : ViewModel() {
    var matchStarted: Boolean = false
    var players: List<Player> = emptyList()
    var serviceSide: Int = 0
    var relaunchSide: Int = 1
    var ttsEnabled: Boolean = false
    var sttEnabled: Boolean = false

    fun startMatch(player1Name: String, player2Name:String, starterId: Int, enableTTS: Boolean, enableSTT: Boolean) {
        matchStarted = true
        players = listOf(Player(player1Name), Player(player2Name))
        serviceSide = starterId
        relaunchSide = when(starterId) {
            0 -> 1
            else -> 0
        }
        ttsEnabled = enableTTS
        sttEnabled = enableSTT
    }
}