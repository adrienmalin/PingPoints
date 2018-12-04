package adrienmalin.pingpoints

import android.arch.lifecycle.ViewModel


class MatchModel : ViewModel() {
    var matchStarted: Boolean = false
    var players: List<Player> = emptyList()
    var serviceSide: Int = 0
    var relaunchSide: Int = 1
    var ttsEnabled: Boolean = false
    var sttEnabled: Boolean = false
    var playId: Int = 0
    var history: MutableList<Point> = ArrayList()

    fun startMatch(player1Name: String, player2Name:String, starterId: Int, enableTTS: Boolean, enableSTT: Boolean) {
        matchStarted = true
        players = listOf(Player(player1Name), Player(player2Name))
        serviceSide = starterId
        relaunchSide = when(serviceSide) {
            0 -> 1
            else -> 0
        }
        ttsEnabled = enableTTS
        sttEnabled = enableSTT
        saveState()
    }

    fun updateScore(scorerId: Int) {
        playId++
        players[scorerId].score++
        if ((players.sumBy { it.score } % 2 == 0) or (players.all { it.score >= 10 })) {
            serviceSide = relaunchSide.also { relaunchSide = serviceSide }
        }
        saveState()
    }

    fun matchFinished(): Boolean {
        val (minScore, maxScore) = players.map { it.score }.sorted()
        return (maxScore >= 11) and (maxScore - minScore >= 2)
    }

    fun matchPoint(): Boolean {
        val (minScore, maxScore) = players.map { it.score }.sorted()
        return (maxScore >= 10) and (maxScore - minScore >= 1)
    }

    fun saveState() {
        val play = Point(players.map { it.score }, serviceSide)
        if (playId == history.size) {
            history.add(play)
        } else {
            history[playId] = play
            history = history.subList(0, playId+1).toMutableList()
        }
    }

    fun undo() {
        playId--
        reloadState()
    }

    fun redo() {
        playId++
        reloadState()
    }

    fun reloadState() {
        history[playId].let{
            players.zip(it.score).forEach{(player, score) -> player.score = score}
            serviceSide = it.serviceSide
            relaunchSide = when(serviceSide) {
                0 -> 1
                else -> 0
            }
        }
    }
}