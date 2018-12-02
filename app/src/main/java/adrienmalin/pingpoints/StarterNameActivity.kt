package adrienmalin.pingpoints

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.*
import android.speech.tts.TextToSpeech
import android.content.Intent
import android.speech.SpeechRecognizer


val CHECK_TTS = 1


class StarterNameActivity : AppCompatActivity() {
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

        // Set HTML text for icons credits
        findViewById<TextView>(R.id.iconsCredit).run {
            setHtmlText(getString(R.string.iconCredits))
            movementMethod = LinkMovementMethod.getInstance()
        }

        // Find views
        player1NameInput = findViewById(R.id.player1Name)
        player2NameInput = findViewById(R.id.player2Name)
        starterRadioGroup = findViewById(R.id.starterRadioGroup)
        enableTtsSwitch = findViewById(R.id.enableTtsSwitch)
        enableSttSwitch = findViewById(R.id.enableSttSwitch)

        enableTtsSwitch?.setOnCheckedChangeListener { view, isChecked -> checkTTS() }
        enableTtsSwitch?.setOnTouchListener { view, event -> checkTTS(); false}

        enableSttSwitch?.setOnCheckedChangeListener { view, isChecked -> checkSTT() }
        enableSttSwitch?.setOnTouchListener { view, event -> checkSTT(); false}

        // Restore
        previousMatch = getPreferences(Context.MODE_PRIVATE)
        previousMatch?.let {
            previousPlayers = it.getStringSet("previousPlayers", emptySet())
            val adapter = ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, previousPlayers.toList())
            player1NameInput?.run {
                setText(
                    it.getString("previousPlayer2", getString(R.string.player_1_default_name)),
                    TextView.BufferType.EDITABLE)
                setAdapter(adapter)
            }
            player2NameInput?.run{
                setText(
                    it.getString("previousPlayer1", getString(R.string.player_2_default_name)),
                    TextView.BufferType.EDITABLE)
                setAdapter(adapter)
            }
            starterRadioGroup?.check(it.getInt("previousStarterId", R.id.radioPlayer1Starts))
            enableTtsSwitch?.isChecked = it.getBoolean("enableTTS", false)
            enableSttSwitch?.isChecked = it.getBoolean("enableSTT", false)
        }
    }

    fun swapNames(view: View) {
        player1NameInput?.text = player2NameInput?.text.also {
            player2NameInput?.text = player1NameInput?.text
        }
    }

    fun checkTTS(){
        enableTtsSwitch?.let {
            if (it.isChecked) {
                Intent().run {
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
                if (resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    Toast.makeText(applicationContext, R.string.TTS_unavailable, Toast.LENGTH_LONG).show()
                    enableTtsSwitch?.isChecked = false
                    Intent().run {
                        action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                        startActivity(this)
                    }
                }
            }
            else -> {
            }
        }
    }

    fun checkSTT(){
        enableSttSwitch?.let {
            if (it.isChecked) {
                if (!SpeechRecognizer.isRecognitionAvailable(this)) {
                    Toast.makeText(applicationContext, R.string.STT_unavailable, Toast.LENGTH_LONG).show()
                    it.isChecked = false
                }
            }
        }
    }

    fun startMatch(view: View) {
        val player1Name = player1NameInput?.text.toString()
        val player2Name = player2NameInput?.text.toString()

        // Save
        previousMatch?.edit()?.run{
            putString("previousPlayer1", player1Name)
            putString("previousPlayer2", player2Name)
            starterRadioGroup?.let{ putInt("previousStarterId", it.checkedRadioButtonId) }
            previousPlayers?.let { putStringSet("previousPlayers", it.plus(player1Name).plus(player2Name)) }
            enableTtsSwitch?.let { putBoolean("enableTTS", it.isChecked) }
            enableSttSwitch?.let { putBoolean("enableSTT", it.isChecked) }
            commit()
        }

        finish()
    }
}
