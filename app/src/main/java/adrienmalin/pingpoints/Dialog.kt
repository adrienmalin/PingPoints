package adrienmalin.pingpoints

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface


class Dialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.dialog_title)
                .setMessage(R.string.dialog_message)
                .setPositiveButton(R.string.go, DialogInterface.OnClickListener { dialog, id ->
                    // FIRE ZE MISSILES!
                })
                .setNegativeButton(R.string.quit, DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                })
                .setView(view)
        // Create the AlertDialog object and return it
        return builder.create()
    }
}