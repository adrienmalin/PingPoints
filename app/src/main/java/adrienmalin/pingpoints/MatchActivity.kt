package adrienmalin.pingpoints

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.View
import android.arch.lifecycle.ViewModelProviders
import android.speech.tts.TextToSpeech
import android.support.design.widget.Snackbar
import android.widget.*

const val REQ_CODE_SPEECH_INPUT = 1


class MatchActivity : AppCompatActivity() {

    var textScore: android.widget.TextView? = null
    var textService: android.widget.TextView? = null
    var buttons: Array<Button> = emptyArray()
    var imageViews: Array<ImageView?> = emptyArray()
    var matchModel: MatchModel? = null
    var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setContentView(R.layout.activity_match)
        setSupportActionBar(findViewById(R.id.toolbar))
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
        val matchModel = ViewModelProviders.of(this).get(MatchModel::class.java)
        if (!matchModel.matchStarted) {
            matchModel.startMatch(
                intent.getStringExtra("player1Name"),
                intent.getStringExtra("player2Name"),
                intent.getIntExtra("starterId", 0),
                intent.getBooleanExtra("enableTTS", false),
                intent.getBooleanExtra("enableSTT", false)
            )
            if (matchModel.ttsEnabled) {
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
        updateUI()
    }

    fun updateUI() {
        matchModel?.apply {
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

    fun updateScore(view: View) {

    }



}
