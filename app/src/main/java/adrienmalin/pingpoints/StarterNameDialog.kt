package adrienmalin.pingpoints

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment

import java.util.ArrayList
import android.app.Activity




class StarterNameDialog : DialogFragment() {
    interface StarterNameDialogListener {
        fun onStaterNameDialogPositiveClick(dialog: DialogFragment)
    }

    var listener: StarterNameDialogListener? = null

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        try {
            listener = activity as StarterNameDialogListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(activity!!.toString() + " must implement StarterNameDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inputPlayer1Name: android.widget.EditText? = findViewById(R.id.input_player_1_name)
        val player1Name = arguments?.getString("PLAYER_1_NAME")
        inputPlayer1Name?.setText(player1Name, TextView.BufferType.EDITABLE)

        val inputPlayer2Name: android.widget.EditText? = findViewById(R.id.input_player_2_name)
        val player2Name = arguments?.getString("PLAYER_2_NAME")
        inputPlayer2Name?.setText(player2Name, TextView.BufferType.EDITABLE)

        val builder = AlertDialog.Builder(activity)
        // Set the dialog title
        builder.setTitle(R.string.new_match)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(0, null)
                .setPositiveButton(R.string.go_button) { dialog, id ->
                    // User clicked OK, so save the mSelectedItems results somewhere
                    // or return them to the component that opened the dialog
                    //...
                }
                .setNegativeButton(R.string.quit_button) { dialog, id ->
                    activity?.finish()
                }

        return builder.create()
    }
}
