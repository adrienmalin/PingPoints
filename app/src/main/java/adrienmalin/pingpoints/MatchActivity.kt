package adrienmalin.pingpoints

import android.arch.lifecycle.ViewModelProviders
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.text.method.LinkMovementMethod
import android.util.Log
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
    var imageViews: Array<ImageView?> = emptyArray()
    var tts: TextToSpeech? = null
    var stt: SpeechRecognizer? = null
    var sttIntent: Intent? = null

    inner class WaitForTtsInit : TextToSpeech.OnInitListener {
        override fun onInit(status: Int) {
            updateUI()
            matchModel?.apply{
                if (sttEnabled) {
                    speakText(
                        getString(
                            R.string.STT_hint,
                            players[0].name,
                            players[1].name
                        ),
                        TextToSpeech.QUEUE_ADD
                    )
                }
            }
        }
    }

    inner class WaitForTtsSpeak : UtteranceProgressListener() {
        override fun onDone(id: String) {
            launchStt()
        }
        override fun onStart(id: String) {}
        override fun onError(id: String) {}
    }

    inner class SttListener : RecognitionListener {
        val LOG_TAG: String = "SttListener"

        override fun onBeginningOfSpeech() {
            Log.i(LOG_TAG, "onBeginningOfSpeech")
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            Log.i(LOG_TAG, "onBufferReceived: $buffer");
        }

        override fun onEndOfSpeech() {
            Log.i(LOG_TAG, "onEndOfSpeech")
        }

        override fun onError(errorCode: Int) {
            val errorMessage: String = when(errorCode) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                SpeechRecognizer.ERROR_SERVER -> "error from server"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Didn't understand, please try again."
            }
            Log.d(LOG_TAG, "FAILED $errorMessage")
            launchStt()
        }

        override fun onEvent(arg0: Int, arg1: Bundle?) {
            Log.i(LOG_TAG, "onEvent")
        }

        override fun onPartialResults(data: Bundle?) {
            //Log.i(LOG_TAG, "onPartialResults")
        }

        override fun onReadyForSpeech(arg0: Bundle?) {
            Log.i(LOG_TAG, "onReadyForSpeech")
        }

        override fun onResults(data: Bundle) {
            Log.i(LOG_TAG, "onResults");
            val results:ArrayList<String> = data.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            var understood = false

            matchModel?.apply {
                for (result in results) {
                    for (player in players) {
                        if (player.pattern?.matcher(result)?.find() == true) {
                            understood = true
                            updateScore(player)
                            updateUI()
                            break
                        }
                    }
                    if (understood) break
                }
                if (!understood) {
                    if (ttsEnabled) {
                        speakText(getString(R.string.not_understood))
                    }
                    else {
                        showText(R.string.not_understood)
                    }
                }
            }
            launchStt()
        }

        override fun onRmsChanged(rmsdB: Float) {
            //Log.i(LOG_TAG, "onRmsChanged: $rmsdB")
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
        //stt?.stopListening()
        tts?.speak(
            text,
            queueMode,
            hashMapOf(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID to "TTS")
        )
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
                        player.pattern = Pattern.compile(this@MatchActivity.getString(R.string.pattern, player.name))
                }
                if (ttsEnabled) {
                    tts = TextToSpeech(this@MatchActivity, WaitForTtsInit())
                }
                if (sttEnabled) {
                    stt = SpeechRecognizer.createSpeechRecognizer(this@MatchActivity).apply {
                        setRecognitionListener(SttListener())
                    }
                    sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().displayLanguage)
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10)
                        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this@MatchActivity.packageName)
                    }
                    if (ttsEnabled) {
                        tts?.setOnUtteranceProgressListener(WaitForTtsSpeak())
                    } else {
                        matchModel?.apply {
                            showText(
                                getString(
                                    R.string.STT_hint,
                                    players[0].name,
                                    players[1].name
                                ),
                                Snackbar.LENGTH_LONG
                            )
                        }
                        launchStt()
                    }
                } else {
                    showText(R.string.button_hint)
                }
            }
        }
        updateUI()
    }

    fun launchStt() {
        matchModel?.apply {
            if (sttEnabled and !matchFinished) {
                try {
                    stt?.startListening(sttIntent)
                } catch (e: ActivityNotFoundException) {
                    sttEnabled = false
                    showText(R.string.STT_unavailable)
                }
            }
        }
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

    override fun onStop() {
        super.onStop()
        tts?.shutdown()
        stt?.destroy()
    }
}
