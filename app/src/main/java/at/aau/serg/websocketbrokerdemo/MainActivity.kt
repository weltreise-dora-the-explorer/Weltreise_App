package at.aau.serg.websocketbrokerdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import at.aau.serg.websocketbrokerdemo.ui.theme.GameOverScreen
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
            if (screen == "host" || screen == "waiting" || screen == "gameover") {
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
                        LobbyScreen(
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateTo("login") }
                        )
                    }
                    "waiting" -> {
                        WaitingScreen(viewModel)
                    }
                    "game" -> {
                        GameScreen(viewModel)
                    }
                    "gameover" -> {
                        val gameOverMessage by viewModel.gameOverMessage.collectAsState()
                        val playerName by viewModel.playerName.collectAsState()
                        GameOverScreen(
                            currentPlayerName = playerName,
                            results = gameOverMessage?.results ?: emptyList(),
                            onPlayAgainClick = { viewModel.playAgain() },
                            onLeaveClick = { viewModel.leaveLobby() }
                        )
                    }
                }
            }
        }
    }
}

