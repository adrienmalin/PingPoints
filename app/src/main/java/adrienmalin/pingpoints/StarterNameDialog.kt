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
    interface StarterNameDialogListener {
        fun setStarterName(serviceSide: Side, names: Collection<String>)
    }

    var mainActivity: StarterNameDialogListener? = null

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        try {
            mainActivity = activity as StarterNameDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity!!.toString() + " must implement StarterNameDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater:LayoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val namesView: View = inflater.inflate(R.layout.starter_name_dialog, null)
        val inputsPlayersNames: Array<EditText?> = arrayOf(
                namesView.findViewById(R.id.inputLeftPlayerName),
                namesView.findViewById(R.id.inputRightPlayerName)
        )
        arguments?.getStringArray("names")?.apply{
            zip(inputsPlayersNames).forEach {
                (name, inputPlayerName) -> inputPlayerName?.setText(name, TextView.BufferType.EDITABLE)
            }
        }

        return AlertDialog.Builder(activity).apply {
            setTitle(R.string.starter_name_dialog_message)
            setView(namesView)
            setPositiveButton(R.string.go_button) { dialog, id ->
                mainActivity?.setStarterName(
                        when ((namesView.findViewById(R.id.radioGroup) as RadioGroup).checkedRadioButtonId) {
                            R.id.radioLeftPlayer -> Side.LEFT
                            else -> Side.RIGHT
                        },
                        inputsPlayersNames.map{ it?.text.toString() }
                )
                dismiss()
            }
            //setNegativeButton(R.string.quit_button) { dialog, id -> activity?.finish() }
        }.create()
    }
}
