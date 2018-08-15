package adrienmalin.pingpoints

import android.content.DialogInterface
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment


class EndOfMatchDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var names: Array<String> = arrayOf("", "")
        var winnerName = ""
        var score = IntArray(2)

        arguments?.apply {
            names = getStringArray("names")
            winnerName = getString("winnerName")
            score = getIntArray("score")
        }

        return AlertDialog.Builder(activity).apply{
            setTitle(getString(R.string.end_match_dialog_title, winnerName))
            setMessage(getString(R.string.score, score[0], score[1]))
            setPositiveButton(
                    R.string.new_match,
                    DialogInterface.OnClickListener { dialog, id ->
                        startActivity(
                                Intent(context, MainActivity::class.java).apply {
                                    putExtra("names", names)
                                }
                        )
                        //activity?.finish()
                    }
            )
            setNeutralButton(
                    R.string.share_button,
                    DialogInterface.OnClickListener { dialog, id ->
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    getString(
                                            R.string.share_subject,
                                            names[Side.LEFT.value],
                                            names[Side.RIGHT.value]
                                    )
                            )
                            putExtra(
                                    Intent.EXTRA_TEXT,
                                    getString(R.string.share_message,
                                            names[Side.LEFT.value],
                                            names[Side.RIGHT.value],
                                            winnerName,
                                            score[0],
                                            score[1]
                                    )
                            )
                            type = "text/plain"
                        }
                        startActivity(sendIntent)
                    }
            )
            /*setNegativeButton(
                    R.string.quit_button,
                    DialogInterface.OnClickListener { dialog, id ->
                        activity?.finish()
                    }
            )*/
        }.create()
    }
}