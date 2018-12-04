package adrienmalin.pingpoints

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.View
import android.arch.lifecycle.ViewModelProviders
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.support.design.widget.Snackbar
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import java.util.*
import java.util.regex.Pattern


class MatchActivity : AppCompatActivity() {
    val REQ_CODE_SPEECH_INPUT = 1

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
                intent.apply {
                    it.startMatch(
                        getStringExtra("player1Name"),
                        getStringExtra("player2Name"),
                        getIntExtra("starterId", 0),
                        getBooleanExtra("enableTTS", false),
                        getBooleanExtra("enableSTT", false)
                    )
                }
                Snackbar.make(
                    findViewById(R.id.coordinatorLayout),
                    R.string.button_hint,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            if (it.ttsEnabled) {
                tts = TextToSpeech(this, TextToSpeech.OnInitListener { fun onInit(status: Int) {} })
                if (it.sttEnabled) tts?.setOnUtteranceProgressListener(WaitForTTS(::launchStt))
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

            if (ttsEnabled) ttsSpeak()

            if (matchFinished()) endMatch()
            else if (sttEnabled and !ttsEnabled) launchStt()
        }
    }

    fun ttsSpeak() {
        matchModel?.apply {
            if (matchFinished()) {
                val (loser, winner) = players.sortedBy { it.score }
                tts?.speak(
                    getString(
                        R.string.victory_speech,
                        winner.name,
                        winner.score,
                        loser.score
                    ),
                    TextToSpeech.QUEUE_FLUSH,
                    hashMapOf(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID to "Victory")
                )
            } else {
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
                if (matchPoint()) {
                    tts?.speak(
                        getString(R.string.match_point),
                        TextToSpeech.QUEUE_ADD,
                        hashMapOf(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID to "MessageId")
                    )
                }
            }
        }
    }

    fun launchStt() {
        matchModel?.apply {
            if (sttEnabled) {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().getDisplayLanguage())
                intent.putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    getString(
                        R.string.STT_hint,
                        players[0].name,
                        players[1].name
                    )
                )
                try {
                    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
                } catch (e: ActivityNotFoundException) {
                    sttEnabled = false
                    Snackbar.make(
                        findViewById(R.id.coordinatorLayout),
                        R.string.STT_unavailable,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                matchModel?.let {
                    var understood: Boolean = false
                    if (resultCode == RESULT_OK && data != null) {
                        val result: String = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)[0]
                        for (player in it.players) {
                            if (Pattern.compile(getString(R.string.pattern, player.name)).matcher(result).find()) {
                                it.updateScore(player)
                                understood = true
                                break
                            }
                        }
                    }
                    if (!understood) launchStt()
                }
            }
            else -> {
            }
        }
    }

    fun updateScore(view: View) {
        matchModel?.apply {
            if (!matchFinished()) {
                for (side in 0..1) {
                    if (view == buttons[side]) {
                        updateScore(players[side])
                    }
                }
                updateUI()
            }
        }
    }

    fun endMatch() {
        matchModel?.let {
            startActivity(
                Intent(this, VictoryActivity::class.java).apply {
                    putExtra("winnerName", it.players.maxBy{ player -> player.score }?.name)
                    putExtra("player1Name", it.players[0].name)
                    putExtra("player1Score", it.players[0].score)
                    putExtra("player2Name", it.players[1].name)
                    putExtra("player2Score", it.players[1].score)
                }
            )
        }
    }

}
