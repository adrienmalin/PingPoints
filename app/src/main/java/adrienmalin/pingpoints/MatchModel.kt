package adrienmalin.pingpoints

import android.arch.lifecycle.ViewModel

class MatchModel : ViewModel() {
    var players: List<Player> = listOf(Player(), Player())
    var server: Int = 0
    var ttsEnabled: Boolean = false
    var sttEnabled: Boolean = false
}