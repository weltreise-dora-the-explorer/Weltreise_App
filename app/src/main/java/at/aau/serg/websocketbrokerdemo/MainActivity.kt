package at.aau.serg.websocketbrokerdemo

import MyStomp
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import at.aau.serg.websocketbrokerdemo.ui.theme.GameScreen
import at.aau.serg.websocketbrokerdemo.ui.theme.LobbyScreen
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import at.aau.serg.websocketbrokerdemo.ui.theme.HostScreen
import at.aau.serg.websocketbrokerdemo.ui.theme.LoginScreen
import at.aau.serg.websocketbrokerdemo.ui.theme.WaitingScreen
import androidx.compose.runtime.mutableIntStateOf

class MainActivity : ComponentActivity(), Callbacks {
    lateinit var myStomp: MyStomp
    lateinit var response: TextView

    private val dice = Dice()

    companion object {
        var lastDiceValue by mutableIntStateOf(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        myStomp = MyStomp(this)
        myStomp.connect()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //Altes Test-UI auskommentiert
      //  setContentView(R.layout.fragment_fullscreen)

    //    findViewById<Button>(R.id.connectbtn).setOnClickListener { myStomp.connect() }
     //   findViewById<Button>(R.id.hellobtn).setOnClickListener { myStomp.sendHello() }
    //    findViewById<Button>(R.id.jsonbtn).setOnClickListener { myStomp.sendJson() }
      //  response = findViewById(R.id.response_view)

    //neues Compose-UI (mit Navigation)
        setContent {
            MaterialTheme{
                var currentScreen by remember {mutableStateOf("login")}

                when (currentScreen) {
                    "login" -> {
                        LoginScreen(
                            onHostClick = { currentScreen = "host" },
                            onJoinClick = { currentScreen = "lobby" }
                        )
                    }
                    "host" -> {
                        HostScreen(
                            onStartClick = { currentScreen = "game" }
                        )
                    }
                    "lobby" -> {
                        LobbyScreen(
                            // GEÄNDERT: Führt jetzt in den Warteraum!
                            onJoinGameClick = { currentScreen = "waiting" }
                        )
                    }
                    "waiting" -> {
                        // NEU: Unser neuer Warteraum! (Alt+Enter drücken, falls es rot leuchtet)
                        WaitingScreen()
                    }
                    "game" -> {
                        GameScreen(
                            diceValue = lastDiceValue,
                            onRollDiceClick = {
                                val rolledValue = dice.roll()
                                lastDiceValue = rolledValue
                                myStomp.sendDiceValue(rolledValue)
                            }
                        )
                    }
                }

            }
        }
    }

    override fun onResponse(res: String) {
        //Serverantwort wird aktuell nicht im UI angezeigt
    }


}

