package adrienmalin.pingpoints

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import kotlin.math.max
import kotlin.math.min


class MatchActivity : AppCompatActivity() {
    var match: MatchModel? = null
    var textScore: android.widget.TextView? = null
    var textService: android.widget.TextView? = null
    var buttons: Array<Button> = emptyArray()
    var imageViews: Array<ImageView> = emptyArray()
    var icStt: ImageView? = null
    var tts: TextToSpeech? = null
    var stt: SpeechRecognizer? = null


    inner class WaitForTtsInit : TextToSpeech.OnInitListener {
        override fun onInit(status: Int) {
            updateUI()
        }
    }

    inner class WaitForTtsSpeak : UtteranceProgressListener() {
        override fun onDone(id: String) {
            runOnUiThread {
                startSTT()
            }
        }

        override fun onStart(id: String) {}
        override fun onError(id: String) {}
    }

    inner class SttListener : RecognitionListener {
        var minRms: Float = 0f
        var maxRms: Float = 0f

        override fun onReadyForSpeech(arg0: Bundle?) {
            icStt?.alpha = .5f
        }

        override fun onBeginningOfSpeech() {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onRmsChanged(rmsdB: Float) {
            minRms = min(rmsdB, minRms)
            maxRms = max(rmsdB, maxRms)
            if (minRms != maxRms)
                icStt?.alpha = 0.5f + (rmsdB - minRms) / (2 * (maxRms - minRms))
        }

        override fun onPartialResults(data: Bundle) {}

        override fun onResults(data: Bundle) {
            stt = null
            data.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { results ->
                match?.apply {
                    for (result in results) {
                        Log.i("stt results", result)
                        val soundex = soundex(result)
                        for (player in players) {
                            if (soundex == player.soundex) {
                                icStt?.alpha = 0f
                                unMuteAudio()
                                updateScore(player)
                                updateUI()
                                return
                            }
                        }
                    }
                }
            }
            showPopUp(getString(R.string.not_understood))
            startSTT()
        }

        override fun onEndOfSpeech() {}

        override fun onError(errorCode: Int) {
            muteAudio()
            Log.e(
                "stt", when (errorCode) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                    SpeechRecognizer.ERROR_SERVER -> "Error from server"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Didn't understand, please try again."
                }
            )
            if (match?.matchStarted == true) startSTT()
        }

        override fun onEvent(arg0: Int, arg1: Bundle?) {}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setContentView(R.layout.activity_match)

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
        icStt = findViewById(R.id.icStt)

        match = ViewModelProvider(this).get(MatchModel::class.java).apply {
            if (!matchStarted) {
                intent.apply {
                    matchStarted = true
                    val player1Name = getStringExtra("player1Name")
                    val player2Name = getStringExtra("player2Name")
                    players = listOf(
                        Player(player1Name ?: getString(R.string.player_1_default_name), 0),
                        Player(player2Name ?: getString(R.string.player_2_default_name), 0)
                    )
                    players.forEach {
                        it.soundex = soundex(getString(R.string.soundex, it.name))
                    }
                    serviceSide = getIntExtra("starterId", 0)
                    relaunchSide = when (serviceSide) {
                        0 -> 1
                        else -> 0
                    }
                    ttsEnabled = getBooleanExtra("enableTTS", false)
                    sttEnabled = getBooleanExtra("enableSTT", false)
                    saveState()

                    if (sttEnabled) {
                        showPopUp(
                            getString(
                                R.string.STT_hint,
                                players[0].name,
                                players[1].name
                            ),
                            Snackbar.LENGTH_LONG
                        )
                    } else {
                        showPopUp(getString(R.string.button_hint))
                    }

                    if (ttsEnabled) {
                        tts = TextToSpeech(this@MatchActivity, WaitForTtsInit()).apply {
                            if (sttEnabled) setOnUtteranceProgressListener(WaitForTtsSpeak())
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        stopSTT()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onPause() {
        stopSTT()
        super.onPause()
    }

    fun updateUI() {
        match?.apply {
            textScore?.text = getString(
                R.string.score_score,
                players[serviceSide].score,
                players[relaunchSide].score
            )
            textService?.text = getString(R.string.service, players[serviceSide].name)

            for ((button, player) in buttons.zip(players))
                button.text = fromHtml(getString(R.string.button_text, player.name, player.score))

            imageViews[0].setImageResource(
                when (serviceSide) {
                    0 -> R.drawable.ic_service_0
                    else -> 0
                }
            )
            imageViews[1].setImageResource(
                when (serviceSide) {
                    0 -> 0
                    else -> R.drawable.ic_service_1
                }
            )

            if (matchStarted) {
                if (matchFinished)
                    proclaimVictory()
                else {
                    if (ttsEnabled) {
                        unMuteAudio()
                        var scoreSpeech = getString(
                            R.string.update_score_speech,
                            players[serviceSide].score,
                            players[relaunchSide].score,
                            players[serviceSide].name
                        )
                        if (matchPoint) scoreSpeech += getString(R.string.match_point)
                        say(scoreSpeech)
                    } else if (sttEnabled) {
                        startSTT()
                    }
                }
            }
        }
    }

    fun proclaimVictory() {
        match?.apply {
            matchStarted = false
            stopSTT()
            val (loser, winner) = players.sortedBy { it.score }
            if (ttsEnabled) {
                say(
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
        }
    }

    fun updateScore(view: View) {
        match?.apply {
            if (!matchFinished) {
                when (view) {
                    buttons[0] -> updateScore(players[0])
                    buttons[1] -> updateScore(players[1])
                }
            }
            updateUI()
        }
    }

    override fun onBackPressed() {
        if (match?.pointId == 0)
            super.onBackPressed()
        else {
            match?.undo()
            updateUI()
        }
    }

    fun showPopUp(text: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(
            findViewById(R.id.coordinatorLayout),
            text,
            duration
        ).show()
    }

    fun showPopUp(textId: Int, duration: Int = Snackbar.LENGTH_INDEFINITE) {
        Snackbar.make(
            findViewById(R.id.coordinatorLayout),
            textId,
            duration
        ).show()
    }

    fun stopSTT() {
        icStt?.alpha = 0f
        unMuteAudio()
        stt?.stopListening()
        stt?.destroy()
        stt = null
    }

    fun startSTT() {
        stt?.stopListening()
        stt?.destroy()
        if (match?.matchStarted == true) {
            stt = SpeechRecognizer.createSpeechRecognizer(this@MatchActivity).apply {
                setRecognitionListener(SttListener())
                try {
                    startListening(
                        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
                            )
                            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10)
                            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                            if (!hasExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE)) {
                                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
                            }
                        }
                    )
                } catch (e: ActivityNotFoundException) {
                    match?.sttEnabled = false
                    showPopUp(R.string.STT_unavailable)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    fun say(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        stopSTT()
        tts?.speak(
            text,
            queueMode,
            hashMapOf(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID to "TTS")
        )
    }

    @Suppress("DEPRECATION")
    fun muteAudio() {
        (getSystemService(Context.AUDIO_SERVICE) as AudioManager).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
                adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0)
                adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
                adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0)
                adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)
            } else {
                setStreamMute(AudioManager.STREAM_NOTIFICATION, true)
                setStreamMute(AudioManager.STREAM_ALARM, true)
                setStreamMute(AudioManager.STREAM_MUSIC, true)
                setStreamMute(AudioManager.STREAM_RING, true)
                setStreamMute(AudioManager.STREAM_SYSTEM, true)
            }
        }
    }

    @Suppress("DEPRECATION")
    fun unMuteAudio() {
        (getSystemService(Context.AUDIO_SERVICE) as AudioManager).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0)
                adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0)
                adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
                adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0)
                adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0)
            } else {
                setStreamMute(AudioManager.STREAM_NOTIFICATION, false)
                setStreamMute(AudioManager.STREAM_ALARM, false)
                setStreamMute(AudioManager.STREAM_MUSIC, false)
                setStreamMute(AudioManager.STREAM_RING, false)
                setStreamMute(AudioManager.STREAM_SYSTEM, false)
            }
        }
    }
}
