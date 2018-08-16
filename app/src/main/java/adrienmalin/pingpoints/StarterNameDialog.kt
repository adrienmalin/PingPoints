package adrienmalin.pingpoints

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView


class StarterNameDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?) =
            AlertDialog.Builder(activity).apply {
                val mainActivity = activity as MainActivity
                val inflater:LayoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val namesView: View = inflater.inflate(R.layout.starter_name_dialog, null)
                val inputsPlayersNames: Array<EditText?> = arrayOf(
                        namesView.findViewById(R.id.inputLeftPlayerName),
                        namesView.findViewById(R.id.inputRightPlayerName)
                )

                inputsPlayersNames.zip(mainActivity.players.map{ it.name }).forEach { (inputPlayerName, name) ->
                    inputPlayerName?.setText(name, TextView.BufferType.EDITABLE)
                }

                setTitle(R.string.starter_name_dialog_message)
                setView(namesView)
                setPositiveButton(R.string.go_button) { dialog, id ->
                    dismiss()
                    mainActivity.setStarterName(
                            when (namesView.findViewById<RadioGroup>(R.id.radioGroup)?.checkedRadioButtonId) {
                                R.id.radioLeftPlayer -> Side.LEFT
                                else -> Side.RIGHT
                            },
                            inputsPlayersNames.map{ it?.text.toString() }
                    )
                }
            }.create()
        }
