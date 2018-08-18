package adrienmalin.pingpoints

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment


class EndOfMatchDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?)=
            AlertDialog.Builder(activity).apply{
                val players = (activity as MainActivity).players
                val names = players.map { it.name }
                val winnerName = players.maxBy { it.score }?.name ?: ""
                val score = players.map { it.score }.sortedDescending()

                setTitle(getString(R.string.end_match_dialog_title, winnerName))
                setMessage(getString(R.string.score, score[0], score[1]))
                setPositiveButton(
                        R.string.new_match,
                        DialogInterface.OnClickListener { dialog, id ->
                            startActivity(
                                    Intent(context, MainActivity::class.java).apply {
                                        putExtra("names", names.toTypedArray())
                                    }
                            )
                        }
                )
                setNeutralButton(
                        R.string.share_button,
                        DialogInterface.OnClickListener { dialog, id ->
                            val newMatchIntent: Intent = Intent().apply {
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
                            startActivity(newMatchIntent)
                        }
                )
            }.create()
        }
