package at.aau.serg.websocketbrokerdemo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.R
import android.view.View

class MainActivity : ComponentActivity(), Callbacks {
    lateinit var myStomp: MyStomp
    lateinit var response: TextView

    private val dice = Dice()
    private var lastDiceValue: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        myStomp = MyStomp(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_fullscreen)

        //Verbindung zum Backend beim Start der Activity herstellen
        myStomp.connect()

        //TextView zur Anzeige des Würfelergebnisses oder Server-Responses
        response = findViewById(R.id.diceResult)

        findViewById<View>(R.id.rollDiceBtn).setOnClickListener {
            //Würfel lokal werfen und Wert speichern
            lastDiceValue = dice.roll()
            //Ergebnis direkt in der UI anzeigen
            response.text = "Würfelergebnis: $lastDiceValue"

            //Vorbereitung: Wert später an den Server senden
            myStomp.sendDiceValue(lastDiceValue)
        }
    }

    override fun onResponse(res: String) {
        //Nachricht vom Server in der UI anzeigen
        response.text = res
    }


}

