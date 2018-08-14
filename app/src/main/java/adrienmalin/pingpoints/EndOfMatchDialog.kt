package adrienmalin.pingpoints

import android.content.DialogInterface
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment


class EndOfMatchDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val player1Name = arguments?.getString("PLAYER_1_NAME")
        val player2Name = arguments?.getString("PLAYER_2_NAME")
        val winnerName = arguments?.getString("WINNER_NAME")
        val winnerScore = arguments?.getInt("WINNER_SCORE")
        val loserScore = arguments?.getInt("LOSER_SCORE")

        builder.setTitle(getString(R.string.end_match_dialog_title, winnerName))
                .setMessage(getString(R.string.score, winnerScore, loserScore))
                .setPositiveButton(
                        R.string.new_match,
                        DialogInterface.OnClickListener {dialog, id ->
                            startActivity(Intent(context, MainActivity::class.java))
                            activity?.finish()
                        }
                )
                .setNeutralButton(
                        R.string.share_button,
                        DialogInterface.OnClickListener { dialog, id ->
                                val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                        Intent.EXTRA_SUBJECT,
                                        getString(
                                                R.string.share_subject,
                                                player1Name,
                                                player2Name
                                        )
                                )
                                putExtra(
                                        Intent.EXTRA_TEXT,
                                        getString(
                                                R.string.share_message,
                                                player1Name,
                                                player2Name,
                                                winnerName,
                                                winnerScore,
                                                loserScore
                                        )
                                )
                                type = "text/plain"
                            }
                            startActivity(sendIntent)
                        }
                )
                .setNegativeButton(
                        R.string.quit_button,
                        DialogInterface.OnClickListener { dialog, id ->
                            activity?.finish()
                        }
                )

        return builder.create()
    }
}