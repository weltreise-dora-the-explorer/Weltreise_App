package at.aau.serg.websocketbrokerdemo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.R
import android.widget.LinearLayout
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

        myStomp.connect()

        response = findViewById(R.id.diceResult)

        findViewById<View>(R.id.rollDiceBtn).setOnClickListener {
            lastDiceValue = dice.roll()
            response.text = "Würfelergebnis: $lastDiceValue"

            myStomp.sendDiceValue(lastDiceValue)
        }
    }

    override fun onResponse(res: String) {
        response.text = res
    }


}

