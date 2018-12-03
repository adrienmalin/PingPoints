package adrienmalin.pingpoints

import android.arch.lifecycle.ViewModel

class MatchModel : ViewModel() {
    lateinit var players: List<Player>
    lateinit var server: Int
    lateinit var ttsEnabled: Boolean
    lateinit var sttEnabled: Boolean

    fun startMatch(player1Name: String, player2Name:String, starterId: Int, enableTTS: Boolean, enableSTT: Boolean) {
        players = listOf(Player(player1Name), Player(player2Name))
        server = starterId
        ttsEnabled = enableTTS
        sttEnabled = enableSTT
    }
}