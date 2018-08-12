package adrienmalin.pingpoints

import android.support.v7.app.AppCompatActivity
import android.os.Bundle


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    val joueurs: Array<Joueur> = arrayOf(Joueur(getString(R.string.nom_joueur_1)), Joueur(getString(R.string.nom_joueur_2)))
    val serveur: Int? = null

}

class Joueur(nom: String, score: Int = 0)