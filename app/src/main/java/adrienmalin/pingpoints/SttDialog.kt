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
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import java.util.*
import kotlin.math.max
import kotlin.math.min

class SttDialog : DialogFragment() {
    var matchActivity: MatchActivity? = null
    var partialResultsTextView: TextView? = null
    var icStt: ImageView? = null
    var stt: SpeechRecognizer? = null
    var sttIntent: Intent? = null

    inner class SttListener : RecognitionListener {
        var minRms: Float = 0f
        var maxRms: Float = 0f

        override fun onRmsChanged(rmsdB: Float) {
            minRms = min(rmsdB, minRms)
            maxRms = max(rmsdB, maxRms)
            if (minRms != maxRms)
                icStt?.alpha = 0.5f + rmsdB / (2*(maxRms - minRms))
        }

        override fun onPartialResults(data: Bundle) {
            partialResultsTextView?.text = data.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)[0]
        }

        override fun onResults(data: Bundle) {
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
                        onError(0)
                    }
                }
            }
        }

        override fun onError(errorCode: Int) {
            partialResultsTextView?.text = getString(R.string.not_understood)
            stt?.startListening(sttIntent)
        }

        override fun onEvent(arg0: Int, arg1: Bundle?) {}
        override fun onReadyForSpeech(arg0: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = AlertDialog.Builder(activity).apply {
        val view: View = (context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.dialog_stt,
            null
        )
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