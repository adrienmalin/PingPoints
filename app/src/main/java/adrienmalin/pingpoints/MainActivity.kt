package adrienmalin.pingpoints

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    val joueurs: Array<Joueur> = arrayOf(Joueur("Joueur 1"), Joueur("Joueur 2"))

    val serveur: Joueur? = null

}

class Joueur(nom: String)