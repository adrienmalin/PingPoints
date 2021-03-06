package adrienmalin.pingpoints

import androidx.lifecycle.ViewModel


class MatchModel : ViewModel() {
    var matchStarted: Boolean = false
    var matchFinished: Boolean = false
    var matchPoint: Boolean = false
    var players: List<Player> = emptyList()
    var serviceSide: Int = 0
    var relaunchSide: Int = 1
    var ttsEnabled: Boolean = false
    var sttEnabled: Boolean = false
    var pointId: Int = -1
    var history: MutableList<Point> = ArrayList()

    fun updateScore(scorer: Player) {
        scorer.score++
        if ((players.sumBy { it.score } % 2 == 0) or (players.all { it.score >= 10 })) {
            serviceSide = relaunchSide.also { relaunchSide = serviceSide }
        }
        saveState()
        updateMatch()
    }

    fun updateMatch() {
        val (minScore, maxScore) = players.map { it.score }.sorted()
        matchFinished = (maxScore >= 11) and (maxScore - minScore >= 2)
        matchPoint = (maxScore >= 10) and (maxScore - minScore >= 1)
    }

    fun saveState() {
        val point = Point(players.map { it.score }, serviceSide)
        if (++pointId == history.size) {
            history.add(point)
        } else {
            history[pointId] = point
        }
    }

    fun undo() {
        history[--pointId].let {
            players.zip(it.score).forEach{(player, score) -> player.score = score}
            serviceSide = it.serviceSide
            relaunchSide = when(serviceSide) {
                0 -> 1
                else -> 0
            }
        }
        updateMatch()
    }
}