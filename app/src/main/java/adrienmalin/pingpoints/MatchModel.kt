package adrienmalin.pingpoints

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel


class MatchModel : ViewModel() {
    var players: List<Player> = emptyList()
    var serverId: Int = 0
    var ttsEnabled: Boolean = false
    var sttEnabled: Boolean = false

    fun startMatch(player1Name: String, player2Name:String, starterId: Int, enableTTS: Boolean, enableSTT: Boolean) {
        players = listOf(Player(player1Name), Player(player2Name))
        serverId = starterId
        ttsEnabled = enableTTS
        sttEnabled = enableSTT
    }
}