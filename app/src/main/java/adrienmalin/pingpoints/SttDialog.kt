package adrienmalin.pingpoints

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import java.util.*

class SttDialog : DialogFragment() {
    var partialResultsTextView: TextView? = null
    var matchActivity: MatchActivity? = null
    var stt: SpeechRecognizer? = null
    var sttIntent: Intent? = null

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
            val errorMessage: String = when (errorCode) {
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
            stt?.startListening(sttIntent)
        }

        override fun onEvent(arg0: Int, arg1: Bundle?) {
            Log.i(LOG_TAG, "onEvent")
        }

        override fun onPartialResults(data: Bundle) {
            //Log.i(LOG_TAG, "onPartialResults")
            partialResultsTextView?.text = data.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)[0]
        }

        override fun onReadyForSpeech(arg0: Bundle?) {
            Log.i(LOG_TAG, "onReadyForSpeech")
        }

        override fun onResults(data: Bundle) {
            Log.i(LOG_TAG, "onResults");
            val results: ArrayList<String> = data.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            var understood = false

            matchActivity?.apply {
                matchModel?.apply {
                    for (result in results) {
                        for (player in players) {
                            if (player.pattern?.matcher(result)?.find() == true) {
                                understood = true
                                dismiss()
                                updateScore(player)
                                updateUI()
                                break
                            }
                        }
                        if (understood) break
                    }
                    if (!understood) {
                        partialResultsTextView?.text = getString(R.string.not_understood)
                        stt?.startListening(sttIntent)
                    }
                }
            }
        }

        override fun onRmsChanged(rmsdB: Float) {
            //Log.i(LOG_TAG, "onRmsChanged: $rmsdB")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = AlertDialog.Builder(activity).apply {
        val view: View = (context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.dialog_stt,
            null
        )
        partialResultsTextView = view.findViewById(R.id.partialResultTextView)

        setView(view)

        matchActivity = (activity as MatchActivity).apply {
            matchModel?.apply {
                view.findViewById<TextView>(R.id.sttHintTextView).text = getString(
                    R.string.STT_hint,
                    players[0].name,
                    players[1].name
                )
                sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().displayLanguage)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }
                stt = SpeechRecognizer.createSpeechRecognizer(activity).apply {
                    setRecognitionListener(SttListener())
                    try {
                        startListening(sttIntent)
                    } catch (e: ActivityNotFoundException) {
                        sttEnabled = false
                        dismiss()
                        showText(R.string.STT_unavailable)
                    }
                }
            }
        }
    }.create()!!

    override fun onStop() {
        super.onStop()
        stt?.destroy()
    }

}