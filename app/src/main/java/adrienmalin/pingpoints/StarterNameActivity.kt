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
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial


class StarterNameActivity : AppCompatActivity() {
    val CHECK_TTS = 1
    val ASK_PERMISSIONS_RECORD_AUDIO = 2

    var player1NameInput: AutoCompleteTextView? = null
    var player2NameInput: AutoCompleteTextView? = null
    var starterRadioGroup: RadioGroup? = null
    var enableTtsSwitch: SwitchMaterial? = null
    var enableSttSwitch: SwitchMaterial? = null
    var previousMatch: SharedPreferences? = null
    var previousPlayers: Set<String> = emptySet()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starter_name)

        findViews()
        checkTtsAvailable()
        checkSttAvailable()
        restorePreviousSettings()
    }

    fun findViews() {
        player1NameInput = findViewById(R.id.player1Name)
        player2NameInput = findViewById(R.id.player2Name)
        starterRadioGroup = findViewById(R.id.starterRadioGroup)
        enableTtsSwitch = findViewById(R.id.enableTtsSwitch)
        enableSttSwitch = findViewById(R.id.enableSttSwitch)
    }

    fun restorePreviousSettings() {
        previousMatch = getPreferences(Context.MODE_PRIVATE).apply {
            getStringSet("previousPlayers", emptySet())?.let { previousPlayers = it.toSet() }
            val adapter = ArrayAdapter(
                this@StarterNameActivity,
                android.R.layout.simple_list_item_1,
                previousPlayers.toList()
            )
            player1NameInput?.apply {
                setText(
                    getString("previousPlayer2", getString(R.string.player_1_default_name)),
                    TextView.BufferType.EDITABLE
                )
                setAdapter(adapter)
            }
            player2NameInput?.apply{
                setText(
                    getString("previousPlayer1", getString(R.string.player_2_default_name)),
                    TextView.BufferType.EDITABLE
                )
                setAdapter(adapter)
            }
            starterRadioGroup?.check(
                when(getInt("previousStarterId", 0)) {
                    1 -> R.id.radioPlayer2Starts
                    else -> R.id.radioPlayer1Starts
                }
            )
            enableTtsSwitch?.isChecked = getBoolean("enableTTS", false)
            enableSttSwitch?.isChecked = getBoolean("enableSTT", false)
        }
    }

    fun checkTtsAvailable() {
        enableTtsSwitch?.setOnCheckedChangeListener  { _, isChecked ->
            if (isChecked)  {
                Intent().apply {
                    action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
                    startActivityForResult(this, CHECK_TTS)
                }
            }
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
                    showText(R.string.TTS_unavailable)
                    Intent().apply {
                        action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                        startActivity(this)
                    }
                }
            } else -> {}
        }
    }

    fun checkSttAvailable() {
        enableSttSwitch?.setOnCheckedChangeListener { _, isChecked ->
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
                    showText(R.string.STT_unavailable)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            ASK_PERMISSIONS_RECORD_AUDIO -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    enableSttSwitch?.isChecked = true
                } else {
                    enableSttSwitch?.isChecked = false
                    showText(R.string.audio_record_permission_denied)
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
        val starterId = when (starterRadioGroup?.checkedRadioButtonId) {
            R.id.radioPlayer2Starts -> 1
            else -> 0
        }
        val enableTTS = enableTtsSwitch?.isChecked
        val enableSTT = enableSttSwitch?.isChecked

        // Save settings
        previousMatch?.edit()?.apply {
            putString("previousPlayer1", player1Name)
            putString("previousPlayer2", player2Name)
            putInt("previousStarterId", starterId)
            putStringSet("previousPlayers", previousPlayers.plus(player1Name).plus(player2Name))
            enableTTS?.let { putBoolean("enableTTS", it) }
            enableSTT?.let { putBoolean("enableSTT", it) }
            apply()
        }

        startActivity(
            Intent(this, MatchActivity::class.java).apply {
                putExtra("player1Name", player1Name)
                putExtra("player2Name", player2Name)
                putExtra("starterId", starterId)
                putExtra("enableTTS", enableTTS)
                putExtra("enableSTT", enableSTT)
            }
        )
    }

    fun showText(textId: Int, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(
            findViewById(R.id.coordinatorLayout),
            textId,
            duration
        ).show()
    }
}
