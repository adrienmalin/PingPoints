package adrienmalin.pingpoints

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.View
import android.arch.lifecycle.ViewModelProviders
import android.speech.tts.TextToSpeech
import android.support.design.widget.Snackbar
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.*

const val REQ_CODE_SPEECH_INPUT = 1


class MatchActivity : AppCompatActivity() {
    var matchModel: MatchModel? = null
    var textScore: android.widget.TextView? = null
    var textService: android.widget.TextView? = null
    var buttons: Array<Button> = emptyArray()
    var imageViews: Array<ImageView?> = emptyArray()
    var tts: TextToSpeech? = null
    var undo: MenuItem? = null
    var redo: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setContentView(R.layout.activity_match)
        setSupportActionBar(findViewById(R.id.toolbar))

        // Set HTML text for icons credits
        findViewById<TextView>(R.id.iconsCredit).run {
            setText(fromHtml(getString(R.string.iconCredits)))
            movementMethod = LinkMovementMethod.getInstance()
        }

        // Find views
        textScore = findViewById(R.id.textScore)
        textService = findViewById(R.id.textService)
        buttons = arrayOf(
            findViewById(R.id.buttonPlayer0),
            findViewById(R.id.buttonPlayer1)
        )
        imageViews = arrayOf(
            findViewById(R.id.imgService0),
            findViewById(R.id.imgService1)
        )

        // Init or restore ViewModel
        matchModel = ViewModelProviders.of(this).get(MatchModel::class.java)
        matchModel?.let {
            if (!it.matchStarted) {
                it.startMatch(
                    intent.getStringExtra("player1Name"),
                    intent.getStringExtra("player2Name"),
                    intent.getIntExtra("starterId", 0),
                    intent.getBooleanExtra("enableTTS", false),
                    intent.getBooleanExtra("enableSTT", false)
                )
                if (it.ttsEnabled) {
                    tts = TextToSpeech(
                        this,
                        TextToSpeech.OnInitListener {
                            fun onInit(status: Int) {}
                        }
                    )
                }
                Snackbar.make(
                    findViewById(R.id.coordinatorLayout),
                    R.string.button_hint,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        updateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.match_menu, menu)
        undo = menu.findItem(R.id.action_undo)
        redo = menu.findItem(R.id.action_redo)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_undo -> {
            matchModel?.undo()
            updateUI()
            true
        }
        R.id.action_redo -> {
            matchModel?.redo()
            updateUI()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    fun updateUI() {
        matchModel?.apply {
            if (matchFinished) {

            } else {
                textScore?.text = getString(
                    R.string.score,
                    players[serviceSide].score,
                    players[relaunchSide].score
                )
                textService?.text = getString(R.string.service, players[serviceSide].name)

                for ((button, player) in buttons.zip(players)) {
                    button.text = fromHtml(getString(R.string.button_text, player.name, player.score))
                }

                when (serviceSide) {
                    0 -> {
                        imageViews[0]?.setImageResource(R.drawable.ic_service_0)
                        imageViews[1]?.setImageResource(0)
                    }
                    else -> {
                        imageViews[0]?.setImageResource(0)
                        imageViews[1]?.setImageResource(R.drawable.ic_service_1)
                    }
                }

                undo?.isVisible = when (playId) {
                    0 -> false
                    else -> true
                }
                redo?.isVisible = when (playId) {
                    history.size - 1 -> false
                    else -> true
                }

                if (ttsEnabled) {
                    tts?.speak(
                        getString(
                            R.string.update_score_speech,
                            players[serviceSide].score,
                            players[relaunchSide].score,
                            players[serviceSide].name
                        ),
                        TextToSpeech.QUEUE_FLUSH,
                        hashMapOf(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID to "MessageId")
                    )
                }
            }
        }
    }

    fun updateScore(view: View) {
        matchModel?.apply {
            if (!matchFinished) {
                for (side in 0..1) {
                    if (view == buttons[side]) {
                        updateScore(side)
                    }
                }
                updateUI()
            }
        }
    }

}
