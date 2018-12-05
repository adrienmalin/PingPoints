package adrienmalin.pingpoints

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.View
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.TextView


class VictoryActivity : AppCompatActivity() {

    var victoryModel: VictoryModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setContentView(R.layout.activity_victory)
        setSupportActionBar(findViewById(R.id.toolbar))

        val previousMatch = getPreferences(Context.MODE_PRIVATE)

        // Init victoryModel
        victoryModel = ViewModelProviders.of(this).get(VictoryModel::class.java)

        victoryModel?.let {
            if (!it.matchFinished) {
                it.matchFinished = true
                it.winnerName = intent.getStringExtra("winnerName")
                it.players = listOf(
                    Player(
                        intent.getStringExtra("player1Name"),
                        intent.getIntExtra("player1Score", 0)
                    ),
                    Player(
                        intent.getStringExtra("player2Name"),
                        intent.getIntExtra("player2Score", 0)
                    )
                )

                it.previousMatches = previousMatch.getString("previousMatches", "")
                previousMatch.edit().apply {
                    putString(
                        "previousMatches",
                        getString(
                            R.string.result,
                            it.players[0].name,
                            it.players[0].score,
                            it.players[1].score,
                            it.players[1].name
                        ) + it.previousMatches
                    )
                    commit()
                }
            }

            // UpdateUI
            findViewById<TextView>(R.id.congrats).text = getString(R.string.congrats, it.winnerName)
            findViewById<TextView>(R.id.player1NameTextView).text = it.players[0].name
            findViewById<TextView>(R.id.scoreTextView).text = getString(
                R.string.score,
                it.players[0].score,
                it.players[1].score
            )
            findViewById<TextView>(R.id.player2NameTextView).text = it.players[1].name
            findViewById<GridView>(R.id.previousMatchesGrid).adapter = ArrayAdapter<String>(
                this,
                R.layout.grid_item,
                R.id.grid_item_text,
                it.previousMatches.split("\t|\n".toRegex())
            )
        }
    }

    fun newMatch(view: View) {
        startActivity(
            Intent(this, StarterNameActivity::class.java)
        )
    }

    fun share(view: View) {
        victoryModel?.let {
            startActivity(
                Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(
                            R.string.share_subject,
                            it.players[0].name,
                            it.players[1].name
                        )
                    )
                    putExtra(
                        Intent.EXTRA_TEXT,
                        getString(
                            R.string.share_message,
                            it.players[0].name,
                            it.players[1].name,
                            it.winnerName,
                            it.players[0].score,
                            it.players[1].score
                        )
                    )
                    type = "text/plain"
                }
            )
        }
    }
}