package adrienmalin.pingpoints

import android.speech.tts.UtteranceProgressListener

class WaitForTTS(val callback: () -> Unit) : UtteranceProgressListener() {
    override fun onDone(id: String) {
        callback()
    }
    override fun onStart(id: String) {}
    override fun onError(id: String) {}
}