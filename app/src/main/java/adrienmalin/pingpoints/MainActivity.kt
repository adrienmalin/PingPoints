package adrienmalin.pingpoints

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button


class MainActivity : AppCompatActivity() {
    var players: Array<Player> = arrayOf(
            Player(),
            Player()
    )
    var server: Int = 0
    var notServer: Int = 1

    var buttonPlayers: Array<Button> = emptyArray()
    var serviceTexts: Array<Array<String>> = arrayOf(
            arrayOf("""_o/°""", ""),
            arrayOf("", """°\o_""")
    )

    var textScore: android.widget.TextView? = null
    var textService: android.widget.TextView? = null
    var stringScore:String = ""
    var stringService:String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val names: Array<String> = resources.getStringArray(R.array.players_names)
        for ((player, name) in players.zip(names)) {
            player.name = name
        }

        textScore = findViewById(R.id.textScore)
        textService = findViewById(R.id.textService)

        stringScore = getString(R.string.score)
        stringService = getString(R.string.service)


        buttonPlayers = arrayOf(
                findViewById(R.id.buttonPlayer1),
                findViewById(R.id.buttonPlayer2)
        )

        update_ui()
    }

    fun updateScore(scoringPlayerId: Int) {
        players[scoringPlayerId].score ++

        if (players.sumBy { it.score } % 2 == 0) {
            server = notServer.also { notServer = server }
        }

        update_ui()
    }

    fun onClickPlayer1(view: View) {
        updateScore(0)
    }

    fun onClickPlayer2(view: View) {
        updateScore(1)
    }

    fun update_ui(){

        textScore?.text = "$stringScore ${players[server].score} - ${players[notServer].score}"

        textService?.text = "$stringService ${players[server].name}"

        for ((player, serviceText) in players.zip(serviceTexts[server])) {
            player.serviceText = serviceText
        }

        for ((button, player) in buttonPlayers.zip(players)) {
            button.text = """
                    |${player.name}
                    |${player.score}
                    |${player.serviceText}""".trimMargin()
        }
    }

}

class Player(
    var name: String = "",
    var score: Int = 0,
    var serviceText: String = ""
)