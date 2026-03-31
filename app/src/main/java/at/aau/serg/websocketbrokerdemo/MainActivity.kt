package at.aau.serg.websocketbrokerdemo

import MyStomp
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.R

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

        response = findViewById(R.id.response_view)

        findViewById<Button>(R.id.connectbtn).setOnClickListener { myStomp.connect() }
        findViewById<Button>(R.id.hellobtn).setOnClickListener {
            lastDiceValue = dice.roll()
            response.text = "Würfelergebnis: $lastDiceValue"
        }
        findViewById<Button>(R.id.jsonbtn).setOnClickListener { myStomp.sendJson() }
    }

    override fun onResponse(res: String) {
        response.text = res
    }


}

