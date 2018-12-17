package adrienmalin.pingpoints

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import java.util.regex.Pattern
import kotlin.math.max
import kotlin.math.min


class SttDialog : DialogFragment() {
    var matchActivity: MatchActivity? = null
    var partialResultsTextView: TextView? = null
    var icStt: ImageView? = null
    var stt: SpeechRecognizer? = null
    var sttIntent: Intent? = null
    var pattern: Pattern? = null

    inner class SttListener : RecognitionListener {
        val ERROR_NOT_UNDERSTOOD = 1
        var minRms: Float = 0f
        var maxRms: Float = 0f

        override fun onReadyForSpeech(arg0: Bundle?) {}

        override fun onBeginningOfSpeech() {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onRmsChanged(rmsdB: Float) {
            minRms = min(rmsdB, minRms)
            maxRms = max(rmsdB, maxRms)
            if (minRms != maxRms)
                icStt?.alpha = 0.5f + (rmsdB - minRms) / (2 * (maxRms - minRms))
        }

        override fun onPartialResults(data: Bundle) {
            data.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { results ->
                matchActivity?.apply {
                    matchModel?.apply {
                        for (result in results) {
                            partialResultsTextView?.text = result
                            pattern?.apply{
                                val matcher = matcher(result)
                                if (matcher.matches()) {
                                    val name_found = matcher.group(1)
                                    for (player in players) {
                                        if (name_found == player.name) {
                                            dismiss()
                                            updateScore(player)
                                            updateUI()
                                            return
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        override fun onResults(data: Bundle) {
            partialResultsTextView?.text = getString(R.string.not_understood)
            onError(ERROR_NOT_UNDERSTOOD)
        }

        override fun onEndOfSpeech() {}

        override fun onError(errorCode: Int) {
            muteAudio()
            stt?.destroy()
            stt = SpeechRecognizer.createSpeechRecognizer(activity).apply {
                setRecognitionListener(this@SttListener)
                startListening(sttIntent)
            }
        }

        override fun onEvent(arg0: Int, arg1: Bundle?) {}
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = AlertDialog.Builder(activity).apply {
        (context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.dialog_stt, null).let { view ->
            partialResultsTextView = view.findViewById(R.id.partialResultTextView)
            icStt = view.findViewById(R.id.icStt)

            setView(view)

            matchActivity = (activity as MatchActivity).apply {
                matchModel?.apply {
                    view.findViewById<TextView>(R.id.sttHintTextView).text = getString(
                        R.string.STT_hint,
                        players[0].name,
                        players[1].name
                    )
                    sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10)
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 30000)
                    }
                    stt = SpeechRecognizer.createSpeechRecognizer(activity).apply {
                        setRecognitionListener(SttListener())
                        try {
                            stopListening()
                            startListening(sttIntent)
                        } catch (e: ActivityNotFoundException) {
                            sttEnabled = false
                            dismiss()
                            showPopUp(R.string.STT_unavailable)
                        }
                    }
                    pattern = Pattern.compile(getString(R.string.pattern), Pattern.CASE_INSENSITIVE)
                }
            }
        }
    }.create()!!

    override fun onResume() {
        super.onResume()

        matchActivity?.apply {
            matchModel?.apply {
                stt?.destroy()
                stt = SpeechRecognizer.createSpeechRecognizer(activity).apply {
                    setRecognitionListener(SttListener())
                    try {
                        stopListening()
                        startListening(sttIntent)
                    } catch (e: ActivityNotFoundException) {
                        sttEnabled = false
                        dismiss()
                        showPopUp(R.string.STT_unavailable)
                    }
                }
            }
        }
    }

    override fun onPause() {
        unMuteAudio()
        stt?.stopListening()
        stt?.destroy()
        super.onPause()
    }

    @Suppress("DEPRECATION")
    fun muteAudio() {
        activity?.apply {
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
    }

    @Suppress("DEPRECATION")
    fun unMuteAudio() {
        activity?.apply {
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
}