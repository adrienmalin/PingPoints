package adrienmalin.pingpoints

import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Button
import android.os.Build
import android.text.Spanned
import android.text.TextUtils.join
import kotlin.math.abs
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.widget.Toast


@SuppressWarnings("deprecation")
fun fromHtml(html: String): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(html)

    }
}


class MainActivity : AppCompatActivity(), StarterNameDialog.StarterNameDialogListener{
    var players: Array<Player> = arrayOf(
            Player(),
            Player()
    )
    var server: Int = 0
    var notServer: Int = 1


    var textScore: android.widget.TextView? = null
    var textService: android.widget.TextView? = null
    var buttons: Array<Button> = emptyArray()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        }

        val defaultNames: Array<String> = resources.getStringArray(R.array.players_names)
        for ((player, defaultName) in players.zip(defaultNames)) {
            player.name = defaultName
        }

        textScore = findViewById(R.id.textScore)
        textService = findViewById(R.id.textService)
        buttons = arrayOf(
                findViewById(R.id.buttonPlayer1),
                findViewById(R.id.buttonPlayer2)
        )

        openStarterNameDialog()

        update_ui()

        Toast.makeText(applicationContext, R.string.info, Snackbar.LENGTH_LONG)
                .show()
    }

    fun openStarterNameDialog() {
        val (loser, winner) = players.sortedBy { it.score }
        var starterNameDialog: EndOfMatchDialog = EndOfMatchDialog()
        starterNameDialog.arguments = Bundle()
        starterNameDialog.arguments?.putString("PLAYER_1_NAME", players[0].name)
        starterNameDialog.arguments?.putString("PLAYER_2_NAME", players[1].name)
        starterNameDialog.show(
                supportFragmentManager,
                join(" ", arrayOf(winner.name, winner.score.toString(), "-", loser.name, loser.score.toString()))
        )
    }

    override fun onStaterNameDialogPositiveClick(dialog: DialogFragment) {
        val inputPlayer1Name: android.widget.EditText? = findViewById(R.id.input_player_1_name)
        players[0].name = inputPlayer1Name?.text.toString()
        val inputPlayer2Name: android.widget.EditText? = findViewById(R.id.input_player_2_name)
        players[1].name = inputPlayer2Name?.text.toString()
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
        updateScore(players[0])
    }

    fun onClickPlayer2(view: View) {
        updateScore(players[1])
    }

    fun finishedMatch() = (
            (players.map { it -> it.score } .max() ?: 0 >= 11) or
            (abs(players[0].score - players[1].score) >= 2)
    )

    fun updateScore(scoringPlayer: Player) {
        if ( !finishedMatch() ) {
            scoringPlayer.score++
            if (players.sumBy { it.score } % 2 == 0) {
                server = notServer.also { notServer = server }
            }
        }
        if ( finishedMatch() ) {
            openEndOfMatchDialog()
        }
        update_ui()
    }

    fun openEndOfMatchDialog() {
        var endOfMatchDialog: EndOfMatchDialog = EndOfMatchDialog()
        val (loser, winner) = players.sortedBy { it.score }
        endOfMatchDialog.arguments = Bundle()
        endOfMatchDialog.arguments?.putString("PLAYER_1_NAME", players[0].name)
        endOfMatchDialog.arguments?.putString("PLAYER_2_NAME", players[1].name)
        endOfMatchDialog.arguments?.putString("WINNER_NAME", winner.name)
        endOfMatchDialog.arguments?.putInt("WINNER_SCORE", winner.score)
        endOfMatchDialog.arguments?.putInt("LOSER_SCORE", loser.score)
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