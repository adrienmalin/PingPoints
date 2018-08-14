package adrienmalin.pingpoints

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Button
import android.os.Build
import android.text.Spanned
import android.text.TextUtils.join
import kotlin.math.abs


@SuppressWarnings("deprecation")
fun fromHtml(html: String): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(html)

    }
}


class MainActivity : AppCompatActivity() {
    var players: Array<Player> = arrayOf(
            Player(),
            Player()
    )
    var server: Int = 0
    var notServer: Int = 1


    var textScore: android.widget.TextView? = null
    var stringScore:String = ""
    var textService: android.widget.TextView? = null
    var stringService:String = ""
    var buttons: Array<Button> = emptyArray()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val names: Array<String> = resources.getStringArray(R.array.players_names)
        for ((player, name) in players.zip(names)) {
            player.name = name
        }

        textScore = findViewById(R.id.textScore)
        textService = findViewById(R.id.textService)
        buttons = arrayOf(
                findViewById(R.id.buttonPlayer1),
                findViewById(R.id.buttonPlayer2)
        )

        update_ui()
    }

    fun update_ui() {

        textScore?.text = getString(R.string.score, players[server].score, players[notServer].score)

        textService?.text = getString(R.string.service, players[server].name)

        for ((button, player) in buttons.zip(players)) {
            button.text = fromHtml(getString(R.string.button_text, player.name, player.score))
        }

        if (server == 0) {
            buttons[0].setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_button, 0, 0, 0)
            buttons[1].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        } else {
            buttons[0].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            buttons[1].setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_button, 0)
        }
    }

    fun onClickPlayer1(view: View) {
        updateScore(0)
    }

    fun onClickPlayer2(view: View) {
        updateScore(1)
    }

    fun updateScore(scoringPlayerId: Int) {
        players[scoringPlayerId].score++

        if (players.sumBy { it.score } % 2 == 0) {
            server = notServer.also { notServer = server }
        }

        update_ui()

        if (
                (players.map { it -> it.score } .max() ?: 0 >= 11) and
                (abs(players[0].score - players[1].score) >= 2)
        ) {
            endOfMatch()
        }
    }

    fun endOfMatch() {
        val (loser, winner) = players.sortedBy { it.score }
        var endOfMatchDialog: EndOfMatchDialog = EndOfMatchDialog()

        val bundle = Bundle()
        bundle.putString("WINNER_NAME", winner.name)
        bundle.putInt("WINNER_SCORE", winner.score)
        bundle.putInt("LOSER_SCORE", loser.score)

        endOfMatchDialog.arguments = bundle
        endOfMatchDialog.show(
                supportFragmentManager,
                join(" ", arrayOf(winner.name, winner.score.toString(), "-", loser.name, loser.score.toString()))
        )
    }

}

data class Player(
    var name: String = "",
    var score: Int = 0,
    var serviceText: String = ""
)