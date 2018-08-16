package adrienmalin.pingpoints

import android.content.Intent
import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.os.Build
import android.support.v7.app.AppCompatDelegate
import android.view.Menu
import android.widget.Toast
import android.view.MenuItem
import android.widget.ImageView


class MainActivity : AppCompatActivity(), StarterNameDialog.StarterNameDialogListener{
    var players: Array<Player> = emptyArray()
    var serviceSide: Side = Side.LEFT
    var relaunchSide: Side = Side.RIGHT
    var textScore: android.widget.TextView? = null
    var textService: android.widget.TextView? = null
    var buttons: Array<Button> = emptyArray()
    var imageViews: Array<ImageView?> = emptyArray()
    var history: ArrayList<State> = ArrayList()
    var step: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
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
        imageViews = arrayOf(
                findViewById(R.id.imgLeftService),
                findViewById(R.id.imgRightService)
        )

        updateUI()

        openStarterNameDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_new_match -> {
            startActivity(
                    Intent(this, MainActivity::class.java).apply {
                        putExtra("names", players.map{ it.name }.toTypedArray())
                    }
            )
            true
        }
        R.id.action_about -> {
            startActivity(Intent(this, CreditsActivity::class.java))
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    fun reloadState(): {
        history[step].apply{
            players.zip(score).forEach{(player, playerScore) -> player.score = playerScore}
            serviceSide = service
            relaunchSide = when(serviceSide) {
                Side.LEFT -> Side.RIGHT
                Side.RIGHT -> Side.LEFT
            }
        }
    }

    fun openStarterNameDialog() {
        StarterNameDialog().apply {
            arguments = Bundle().apply {
                putStringArray("names", players.map{ it.name }.toTypedArray())
            }
            show( supportFragmentManager, "StarterNameDialog")
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
                imageViews[Side.LEFT.value]?.setImageResource(R.drawable.ic_left_service)
                imageViews[Side.RIGHT.value]?.setImageResource(0)
            }
            Side.RIGHT -> {
                imageViews[Side.LEFT.value]?.setImageResource(0)
                imageViews[Side.RIGHT.value]?.setImageResource(R.drawable.ic_right_service)
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
        val state = State(players.map { it.score }, serviceSide)
        if (step >= history.size) {
            history.add(state)
        } else {
            history.removeAt(step + 1)
            history[step] = state
        }
        step ++

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
            show(supportFragmentManager,"EndOfMatchDialog")
        }
    }
}