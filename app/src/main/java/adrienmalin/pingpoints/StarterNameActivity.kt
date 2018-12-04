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
        setSupportActionBar(findViewById(R.id.toolbar))

        // Find views
        player1NameInput = findViewById(R.id.player1Name)
        player2NameInput = findViewById(R.id.player2Name)
        starterRadioGroup = findViewById(R.id.starterRadioGroup)
        enableTtsSwitch = findViewById(R.id.enableTtsSwitch)
        enableSttSwitch = findViewById(R.id.enableSttSwitch)
        // Check if function is available on switch checked or swapped
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
                    Snackbar.make(
                        findViewById(R.id.coordinatorLayout),
                        R.string.TTS_unavailable,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    enableTtsSwitch?.isChecked = false
                    Intent().run {
                        action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                        startActivity(this)
                    }
                }
            } else -> {
            }
        }
    }

    fun checkSTT(){
        enableSttSwitch?.let {
            if (it.isChecked) {
                if (!SpeechRecognizer.isRecognitionAvailable(this)) {
                    Snackbar.make(
                        findViewById(R.id.coordinatorLayout),
                        R.string.STT_unavailable,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    enableSttSwitch?.isChecked = false
                } else {
                    // Ask for record audio permission
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        // Permission is not granted
                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                            AlertDialog.Builder(this)
                                .setTitle(R.string.STT)
                                .setMessage(R.string.explain_record_audio_request)
                                .setPositiveButton(R.string.OK) { dialog, id ->
                                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), ASK_PERMISSIONS_RECORD_AUDIO)
                                }
                                .setNegativeButton(R.string.cancel) { dialog, id ->
                                    enableSttSwitch?.isChecked = false
                                }
                                .create()
                                .show()
                        } else {
                            // No explanation needed, we can request the permission.
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), ASK_PERMISSIONS_RECORD_AUDIO)
                        }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            ASK_PERMISSIONS_RECORD_AUDIO -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
                    // permission denied
                    Snackbar.make(
                        findViewById(R.id.coordinatorLayout),
                        R.string.audio_record_permission_denied,
                        Snackbar.LENGTH_LONG
                    ).show()
                    enableSttSwitch?.isChecked = false
                }
                return
            } else -> {
            }
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

        finish()
    }
}
