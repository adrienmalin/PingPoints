package adrienmalin.pingpoints

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.widget.*


class StarterNameActivity : AppCompatActivity() {
    val CHECK_TTS = 1
    val ASK_PERMISSIONS_RECORD_AUDIO = 2

    var player1NameInput: AutoCompleteTextView? = null
    var player2NameInput: AutoCompleteTextView? = null
    var starterRadioGroup: RadioGroup? = null
    var enableTtsSwitch: Switch? = null
    var enableSttSwitch: Switch? = null
    var previousMatch: SharedPreferences? = null
    var previousPlayers: Set<String> = emptySet()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starter_name)

        // Find views
        player1NameInput = findViewById(R.id.player1Name)
        player2NameInput = findViewById(R.id.player2Name)
        starterRadioGroup = findViewById(R.id.starterRadioGroup)
        enableTtsSwitch = findViewById(R.id.enableTtsSwitch)
        enableSttSwitch = findViewById(R.id.enableSttSwitch)

        // Restore previous data
        previousMatch = getPreferences(Context.MODE_PRIVATE)
        previousMatch?.apply {
            previousPlayers = getStringSet("previousPlayers", emptySet())
            val adapter = ArrayAdapter<String>(
                this@StarterNameActivity,
                android.R.layout.simple_list_item_1,
                previousPlayers.toList())
            player1NameInput?.run {
                setText(
                    getString("previousPlayer2", getString(R.string.player_1_default_name)),
                    TextView.BufferType.EDITABLE)
                setAdapter(adapter)
            }
            player2NameInput?.run{
                setText(
                    getString("previousPlayer1", getString(R.string.player_2_default_name)),
                    TextView.BufferType.EDITABLE)
                setAdapter(adapter)
            }
            starterRadioGroup?.check(getInt("previousStarterId", R.id.radioPlayer1Starts))
            enableTtsSwitch?.isChecked = getBoolean("enableTTS", false)
            enableSttSwitch?.isChecked = getBoolean("enableSTT", false)
        }

        // Check if function is available on switch checked or swapped
        enableTtsSwitch?.setOnCheckedChangeListener  { _, isChecked ->
        if (isChecked)  {
                Intent().apply {
                    action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
                    startActivityForResult(this, CHECK_TTS)
                }
            }
            false
        }

        enableSttSwitch?.setOnCheckedChangeListener  { _, isChecked ->
        if (isChecked) {
                if (SpeechRecognizer.isRecognitionAvailable(this@StarterNameActivity)) {
                    // Ask for record audio permission
                    if (ContextCompat.checkSelfPermission(
                            this@StarterNameActivity,
                            Manifest.permission.RECORD_AUDIO
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@StarterNameActivity,
                            arrayOf(Manifest.permission.RECORD_AUDIO),
                            ASK_PERMISSIONS_RECORD_AUDIO
                        )
                    }
                } else {
                    enableSttSwitch?.isChecked = false
                    Snackbar.make(
                        findViewById(R.id.coordinatorLayout),
                        R.string.STT_unavailable,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CHECK_TTS -> {
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    enableTtsSwitch?.isChecked = true
                } else {
                    enableTtsSwitch?.isChecked = false
                    Snackbar.make(
                        findViewById(R.id.coordinatorLayout),
                        R.string.TTS_unavailable,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    Intent().apply {
                        action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                        startActivity(this)
                    }
                }
            } else -> {}
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            ASK_PERMISSIONS_RECORD_AUDIO -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    enableSttSwitch?.isChecked = true
                } else {
                    enableSttSwitch?.isChecked = false
                    Snackbar.make(
                        findViewById(R.id.coordinatorLayout),
                        R.string.audio_record_permission_denied,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } else -> {}
        }
    }

    fun swapNames(view: View) {
        player1NameInput?.text = player2NameInput?.text.also {
            player2NameInput?.text = player1NameInput?.text
        }
    }

    fun startMatch(view: View) {
        val player1Name = player1NameInput?.text.toString()
        val player2Name = player2NameInput?.text.toString()
        val radioStarterId = starterRadioGroup?.checkedRadioButtonId
        val enableTTS = enableTtsSwitch?.isChecked
        val enableSTT = enableSttSwitch?.isChecked

        // Save
        previousMatch?.edit()?.apply{
            player1Name.let { putString("previousPlayer1", it) }
            player2Name.let { putString("previousPlayer2", it) }
            radioStarterId?.let{ putInt("previousStarterId", it) }
            putStringSet("previousPlayers", previousPlayers.plus(player1Name).plus(player2Name))
            enableTTS?.let { putBoolean("enableTTS", it) }
            enableSTT?.let { putBoolean("enableSTT", it) }
            commit()
        }

        startActivity(
            Intent(this, MatchActivity::class.java).apply {
                putExtra("player1Name", player1Name)
                putExtra("player2Name", player2Name)
                putExtra(
                    "starterId",
                    when(radioStarterId) {
                        R.id.radioPlayer2Starts -> 1
                        else -> 0
                    }
                )
                putExtra("enableTTS", enableTTS)
                putExtra("enableSTT", enableSTT)
            }
        )
    }
}
