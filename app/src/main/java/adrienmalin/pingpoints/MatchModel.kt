package adrienmalin.pingpoints

class MatchModel : ViewModel() {
    var players: List<Player> = ListOf(Player(), Player())
    var server: Int = 0
    var ttsEnabled: Boolean = False
    var sttEnabled: Boolean = False
}