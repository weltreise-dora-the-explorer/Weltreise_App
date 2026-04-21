package at.aau.serg.websocketbrokerdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import at.aau.serg.websocketbrokerdemo.ui.theme.GameScreen
import at.aau.serg.websocketbrokerdemo.ui.theme.HostScreen
import at.aau.serg.websocketbrokerdemo.ui.theme.LobbyScreen
import at.aau.serg.websocketbrokerdemo.ui.theme.LoginScreen
import at.aau.serg.websocketbrokerdemo.ui.theme.WaitingScreen

class MainActivity : ComponentActivity() {
    
    private val viewModel: AppViewModel by viewModels()

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            val screen = viewModel.currentScreen.value
            if (screen == "host" || screen == "waiting") {
                viewModel.leaveLobby()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                
                when (currentScreen) {
                    "login" -> {
                        LoginScreen(
                            onHostClick = { typedName -> viewModel.hostLobby(typedName) },
                            onJoinClick = { typedName -> 
                                viewModel.setPlayerName(typedName)
                                viewModel.navigateTo("lobby") 
                            }
                        )
                    }
                    "host" -> {
                        HostScreen(viewModel)
                    }
                    "lobby" -> {
                        LobbyScreen(viewModel)
                    }
                    "waiting" -> {
                        WaitingScreen(viewModel)
                    }
                    "game" -> {
                        GameScreen(viewModel)
                    }
                }
            }
        }
    }
}

