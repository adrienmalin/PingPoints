package adrienmalin.pingpoints

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.View
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
                it.player1Name = intent.getStringExtra("player1Name")
                it.player2Name = intent.getStringExtra("player2Name")
                it.score = intent.getStringExtra("score")

                it.previousMatches = previousMatch.getString("previousMatches", "")
                previousMatch.edit().apply {
                    putString(
                        "previousMatches",
                        getString(
                            R.string.score_names,
                            it.player1Name,
                            it.score,
                            it.player2Name
                        ) + '\n' + it.previousMatches
                    )
                    commit()
                }
            }

            // UpdateUI
            findViewById<TextView>(R.id.congrats).text = getString(R.string.congrats, it.winnerName)
            findViewById<TextView>(R.id.scoreNames).text = getString(R.string.score_names, it.player1Name, it.score, it.player2Name)
            findViewById<TextView>(R.id.previousMatches).text = it.previousMatches
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
                            it.player1Name,
                            it.player2Name
                        )
                    )
                    putExtra(
                        Intent.EXTRA_TEXT,
                        getString(
                            R.string.share_message,
                            it.player1Name,
                            it.player2Name,
                            it.winnerName,
                            it.score
                        )
                    )
                    type = "text/plain"
                }
            )
        }
    }
}