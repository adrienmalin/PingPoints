package adrienmalin.pingpoints

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*


class StarterNameActivity : AppCompatActivity() {
    var player1NameInput: AutoCompleteTextView = null
    var player2NameInput: AutoCompleteTextView = null
    var starterRadioGroup: RadioGroup = null
    var previousMatch: SharedPreferences = null
    var previousPlayers: Set<String> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starter_name)

        previousMatch = getPreferences(Context.MODE_PRIVATE)
        previousPlayers = previousMatch.getStringSet("previousPlayers", emptySet())
        val previousPlayersAdapter = ArrayAdapter<String>(
            this,
            R.layout.activity_starter_name,
            previousPlayers.toList())

        player1NameInput = findViewById<AutoCompleteTextView>(R.id.player1Name)
        player1NameInput?.run {
            setText(
                previousMatch.getString(
                    "previousPlayer2",
                    getString(R.string.player_1_default_name)),
                TextView.BufferType.EDITABLE)
            setAdapter(previousPlayersAdapter)
            threshold = 1
        }

        player2NameInput = findViewById<AutoCompleteTextView>(R.id.player2Name)
        player2NameInput?.run{
            setText(
                previousMatch.getString(
                    "previousPlayer1",
                    getString(R.string.player_2_default_name)),
                TextView.BufferType.EDITABLE)
            setAdapter(previousPlayersAdapter)
            threshold = 1
        }

        starterRadioGroup = findViewById<RadioGroup>(R.id.starterRadioGroup)
        starterRadioGroup?.check(previousMatch.getInt("previousStarterId", 0))
    }

    fun swapNames(view: View) {
        player1NameInput?.text = player2NameInput?.text.also {
            player2NameInput?.text = player1NameInput?.text
        }
    }

    fun startMatch(view: View) {
        val player1Name = player1NameInput?.text.toString()
        val player2Name = player2NameInput?.text.toString()

        // Save
        previousMatch.edit().run{
            putString("previousPlayer1", player1Name)
            putString("previousPlayer2", player2Name)
            putInt("previousStarterId", starterRadioGroup?.checkedRadioButtonId)
            putStringSet(
                "previousPlayers",
                previousPlayers.plus(player1Name).plus(player2Name))
            commit()
        }
    }
}
