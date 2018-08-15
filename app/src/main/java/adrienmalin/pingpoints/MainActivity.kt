package adrienmalin.pingpoints

import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.os.Build
import android.text.TextUtils.join
import android.widget.Toast


class MainActivity : AppCompatActivity(), StarterNameDialog.StarterNameDialogListener{
    var players: Array<Player> = emptyArray()
    var serviceSide: Side = Side.LEFT
    var relaunchSide: Side = Side.RIGHT

    var textScore: android.widget.TextView? = null
    var textService: android.widget.TextView? = null
    var buttons: Array<Button> = emptyArray()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }

        var names: Array<String> = intent.getStringArrayExtra("names") ?: resources.getStringArray(R.array.default_players_names)
        players = arrayOf(
                Player(names[Side.LEFT.value]),
                Player(names[Side.RIGHT.value])
        )

        textScore = findViewById(R.id.textScore)
        textService = findViewById(R.id.textService)
        buttons = arrayOf(
                findViewById(R.id.buttonLeftPlayer),
                findViewById(R.id.buttonRightPlayer)
        )

        updateUI()

        openStarterNameDialog()
    }

    fun openStarterNameDialog() {
        StarterNameDialog().apply {
            val names = players.map{ it.name }.toTypedArray()
            arguments = Bundle().apply {
                putStringArray("names", names)
            }
            show(
                supportFragmentManager,
                "StarterNameDialog:" + join(" vs. ", names)
            )
        }
    }

    override fun setStarterName(serviceSide: Side, names: Collection<String>) {
        players.zip(names).forEach { (player, name) -> player.name = name}
        this.serviceSide = serviceSide
        relaunchSide = when(serviceSide) {
            Side.LEFT -> Side.RIGHT
            Side.RIGHT -> Side.LEFT
        }

        updateUI()
        Toast.makeText(applicationContext, R.string.info, Toast.LENGTH_LONG).show()
    }

    fun updateUI() {
        textScore?.text = getString(R.string.score, players[serviceSide.value].score, players[relaunchSide.value].score)
        textService?.text = getString(R.string.service, players[serviceSide.value].name)

        for ((button, player) in buttons.zip(players)) {
            button.text = fromHtml(getString(R.string.button_text, player.name, player.score))
        }

        when (serviceSide) {
            Side.LEFT -> {
                buttons[Side.LEFT.value].setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_left_service, 0, 0, 0)
                buttons[Side.RIGHT.value].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
            Side.RIGHT -> {
                buttons[Side.LEFT.value].setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                buttons[Side.RIGHT.value].setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right_service, 0)
            }
        }
    }

    fun onClickLeftPlayer(view: View) {
        updateScore(players[Side.LEFT.value])
    }

    fun onClickRightPlayer(view: View) {
        updateScore(players[Side.RIGHT.value])
    }

    fun updateScore(scoringPlayer: Player) {
        if ( !matchIsFinished() ) {
            scoringPlayer.score++
            if (players.sumBy { it.score } % 2 == 0) {
                serviceSide = relaunchSide.also { relaunchSide = serviceSide }
            }
        }
        if ( matchIsFinished() ) {
            openEndOfMatchDialog()
        }
        updateUI()
    }

    fun matchIsFinished(): Boolean {
        val (minScore, maxScore) = players.map { it.score }.sorted()
        return (maxScore >= 11) and (maxScore - minScore >= 2)
    }

    fun openEndOfMatchDialog() {
        EndOfMatchDialog().apply {
            arguments = Bundle().apply {
                putStringArray("names", players.map{ it.name }.toTypedArray())
                putString("winnerName", players.maxBy { it.score }?.name)
                putIntArray("score", players.map{ it.score }.sortedDescending().toIntArray())
            }
            show(
                    supportFragmentManager,
                    "EndOfMatchDialog"
            )
        }
    }
}