package adrienmalin.pingpoints

import android.arch.lifecycle.ViewModelProviders
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import java.util.*
import java.util.regex.Pattern


class MatchActivity : AppCompatActivity() {
    var matchModel: MatchModel? = null
    var textScore: android.widget.TextView? = null
    var textService: android.widget.TextView? = null
    var buttons: Array<Button> = emptyArray()
    var imageViews: Array<ImageView> = emptyArray()
    var tts: TextToSpeech? = null

    inner class WaitForTtsInit : TextToSpeech.OnInitListener {
        override fun onInit(status: Int) {
            updateUI()
        }
    }

    inner class SttAfterTts : UtteranceProgressListener() {
        override fun onDone(id: String) {
            SttDialog().show( supportFragmentManager, "SttDialog")
        }
        override fun onStart(id: String) {}
        override fun onError(id: String) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setContentView(R.layout.activity_match)

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
        matchModel?.apply {
            if (!matchStarted) {
                intent.apply {
                    startMatch(
                        getStringExtra("player1Name"),
                        getStringExtra("player2Name"),
                        getIntExtra("starterId", 0),
                        getBooleanExtra("enableTTS", false),
                        getBooleanExtra("enableSTT", false)
                    )
                    for (player in players)
                        player.pattern = Pattern.compile(getString(R.string.pattern, player.name))
                }
                if (ttsEnabled) {
                    tts = TextToSpeech(this@MatchActivity, WaitForTtsInit())
                    if (sttEnabled)
                        tts?.setOnUtteranceProgressListener(SttAfterTts())
                }
                if (!sttEnabled){
                    showText(getString(R.string.button_hint))
                }
            }
        }
        updateUI()
    }

    override fun onBackPressed() {
        if (matchModel?.pointId == 0)
            super.onBackPressed()
        else {
            matchModel?.undo()
            updateUI()
        }
    }

    fun updateUI() {
        matchModel?.apply {
            textScore?.text = getString(
                R.string.score_score,
                players[serviceSide].score,
                players[relaunchSide].score
            )
            textService?.text = getString(R.string.service, players[serviceSide].name)

            imageViews[0]?.setImageResource(
                when(serviceSide) {
                    0 -> R.drawable.ic_service_0
                    else -> 0
                }
            )

            for ((button, player) in buttons.zip(players)) {
                button.text = fromHtml(getString(R.string.button_text, player.name, player.score))
            }

            imageViews[1]?.setImageResource(
                when(serviceSide) {
                    0 -> 0
                    else -> R.drawable.ic_service_1
                }
            )

            if (matchFinished) {
                val (loser, winner) = players.sortedBy { it.score }
                if (sttEnabled) {
                    speakText(
                        getString(
                            R.string.victory_speech,
                            winner.name,
                            winner.score,
                            loser.score
                        )
                    )
                }
                startActivity(
                    Intent(this@MatchActivity, VictoryActivity::class.java).apply {
                        putExtra("winnerName", winner.name)
                        putExtra("player1Name", players[0].name)
                        putExtra("player2Name", players[1].name)
                        putExtra("player1Score", players[0].score)
                        putExtra("player2Score", players[1].score)
                    }
                )
            } else {
                if (ttsEnabled) {
                    var scoreSpeech: String = getString(
                        R.string.update_score_speech,
                        players[serviceSide].score,
                        players[relaunchSide].score,
                        players[serviceSide].name
                    )
                    if (matchPoint)
                        scoreSpeech += getString(R.string.match_point)
                    speakText(scoreSpeech)
                } else {
                    if (sttEnabled)
                        SttDialog().show(supportFragmentManager, "SttDialog")
                }
            }
        }
    }

    fun updateScore(view: View) {
        matchModel?.apply {
            if (!matchFinished) {
                for (side in 0..1) {
                    if (view == buttons[side]) {
                        updateScore(players[side])
                    }
                }
                updateUI()
            }
        }
    }

    fun showText(text: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(
            findViewById(R.id.coordinatorLayout),
            text,
            duration
        ).show()
    }

    fun showText(textId: Int, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(
            findViewById(R.id.coordinatorLayout),
            textId,
            duration
        ).show()
    }

    fun speakText(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        tts?.speak(
            text,
            queueMode,
            hashMapOf(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID to "TTS")
        )
    }
}
