package adrienmalin.pingpoints

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import kotlin.math.max
import kotlin.math.min


class VictoryActivity : AppCompatActivity() {
    var victoryModel: VictoryModel? = null
    var previousMatch: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setContentView(R.layout.activity_victory)

        previousMatch = getPreferences(Context.MODE_PRIVATE)
        initVictoryModel()
        updateUI()
    }

    fun initVictoryModel() {
        victoryModel = ViewModelProvider(this).get(VictoryModel::class.java).apply {
            if (!matchFinished) {
                matchFinished = true
                winnerName = intent.getStringExtra("winnerName") ?: ""
                players = listOf(
                    Player(
                        intent.getStringExtra("player1Name")
                            ?: getString(R.string.player_1_default_name),
                        intent.getIntExtra("player1Score", 0)
                    ),
                    Player(
                        intent.getStringExtra("player2Name")
                            ?: getString(R.string.player_2_default_name),
                        intent.getIntExtra("player2Score", 0)
                    )
                )
                previousMatch?.apply{ previousMatches = getString("previousMatches", "") ?: "" }
            }
        }
    }

    fun updateUI() {
        victoryModel?.apply {
            findViewById<TextView>(R.id.congrats).text = getString(R.string.congrats, winnerName)
            findViewById<TextView>(R.id.player1NameTextView).text = players[0].name
            findViewById<TextView>(R.id.scoreTextView).text = getString(
                R.string.score,
                players[0].score,
                players[1].score
            )
            findViewById<TextView>(R.id.player2NameTextView).text = players[1].name
            findViewById<GridView>(R.id.previousMatchesGrid).adapter = ArrayAdapter(
                this@VictoryActivity,
                R.layout.grid_item,
                R.id.grid_item_text,
                previousMatches.split("[\t\n]".toRegex()).toMutableList()
            )
        }

        // Set HTML text for icons credits
        findViewById<TextView>(R.id.iconsCredit).apply {
            text = fromHtml(getString(R.string.iconCredits))
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    override fun onStop() {
        victoryModel?.apply {
            previousMatch?.edit()?.apply {
                putString(
                    "previousMatches",
                    getString(
                        R.string.results,
                        players[0].name,
                        "%2d - %2d".format(players[0].score, players[1].score),
                        players[1].name,
                        previousMatches
                    )
                )
                apply()
            }
        }
        super.onStop()
    }

    fun newMatch(@Suppress("UNUSED_PARAMETER") view: View) {
        startActivity(
            Intent(this, StarterNameActivity::class.java)
        )
    }

    fun share(@Suppress("UNUSED_PARAMETER") view: View) {
        victoryModel?.apply {
            startActivity(
                Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(
                            R.string.share_subject,
                            players[0].name,
                            players[1].name
                        )
                    )
                    putExtra(
                        Intent.EXTRA_TEXT,
                        getString(
                            R.string.share_message,
                            players[0].name,
                            players[1].name,
                            winnerName,
                            max(players[0].score, players[1].score),
                            min(players[0].score, players[1].score)
                        )
                    )
                    type = "text/plain"
                }
            )
        }
    }
}