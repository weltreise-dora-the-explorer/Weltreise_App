package at.aau.serg.websocketbrokerdemo

import MyStomp
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

open class AppViewModel(stompInstance: MyStomp? = null) : ViewModel(), Callbacks {
    open val stomp: MyStomp = stompInstance ?: MyStomp(this)

    private val _currentScreen = MutableStateFlow("login")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _lobbyId = MutableStateFlow("")
    val lobbyId: StateFlow<String> = _lobbyId.asStateFlow()

    private val _playerName = MutableStateFlow("")
    val playerName: StateFlow<String> = _playerName.asStateFlow()

    private val _playersList = MutableStateFlow<List<String>>(emptyList())
    val playersList: StateFlow<List<String>> = _playersList.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isHost = MutableStateFlow(false)
    val isHost: StateFlow<Boolean> = _isHost.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _gameMode = MutableStateFlow("Grand Tour")
    val gameMode: StateFlow<String> = _gameMode.asStateFlow()

    fun setGameMode(mode: String) {
        _gameMode.value = mode
    }

    init {
        // Verbinde sofort mit dem Server beim Startfenster (nur wenn kein Mock injiziert)
        if (stompInstance == null) {
            stomp.connect()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun setPlayerName(name: String) {
        _playerName.value = name
    }

    fun joinLobby(pin: String) {
        _lobbyId.value = pin
        _isHost.value = false
        _isLoading.value = true
        _errorMessage.value = null
        stomp.joinMultiplayerLobby(pin, _playerName.value)
        // Navigation passiert jetzt in onResponse() nach Server-Bestätigung
    }

    fun hostLobby(name: String) {
        val randomPin = (1000..9999).random().toString()
        _lobbyId.value = randomPin
        _playerName.value = name
        _isHost.value = true
        _isLoading.value = true
        _errorMessage.value = null
        stomp.createMultiplayerLobby(randomPin, name)
        // Navigation passiert jetzt in onResponse() nach Server-Bestätigung
    }

    fun startGame() {
        val stops = when (_gameMode.value) {
            "City Hopper" -> 6
            "Epic Voyage" -> 18
            else -> 12
        }
        stomp.startGameCmd(_lobbyId.value, stops)
    }

    fun leaveLobby() {
        val currentLobbyId = _lobbyId.value
        val currentPlayerName = _playerName.value
        if (currentLobbyId.isNotBlank() && currentPlayerName.isNotBlank()) {
            stomp.leaveLobby(currentLobbyId, currentPlayerName)
        }
        _lobbyId.value = ""
        _playersList.value = emptyList()
        _isHost.value = false
        navigateTo("login")
    }

    override fun onResponse(res: String) {
        Log.i("AppViewModel", "Received from server: $res")
        _isLoading.value = false

        try {
            if (res.startsWith("{")) {
                val rootJson = JSONObject(res)

                // Prüfe success-Flag für Error-Handling
                if (rootJson.has("success") && !rootJson.getBoolean("success")) {
                    val errorMsg = rootJson.optString("message", "Unbekannter Fehler")
                    _errorMessage.value = errorMsg
                    Log.e("AppViewModel", "Server-Fehler: $errorMsg")
                    return
                }

                // Erfolgreiche Response - navigiere je nach Kontext
                if (rootJson.has("state") && !rootJson.isNull("state")) {
                    val stateJson = rootJson.getJSONObject("state")

                    // Spielerliste aktualisieren
                    if (stateJson.has("players")) {
                        val playersArray = stateJson.getJSONArray("players")
                        val newList = mutableListOf<String>()
                        for (i in 0 until playersArray.length()) {
                            val playerObj = playersArray.getJSONObject(i)
                            newList.add(playerObj.getString("playerId"))
                        }
                        _playersList.value = newList
                    }

                    // Navigation basierend auf Phase und Command-Type
                    val commandType = rootJson.optString("commandType", "")
                    val phase = stateJson.optString("phase", "LOBBY")

                    when {
                        phase != "LOBBY" -> {
                            // Spiel hat gestartet!
                            navigateTo("game")
                        }
                        commandType == "CREATE_LOBBY" -> {
                            // Host hat Lobby erstellt
                            navigateTo("host")
                        }
                        commandType == "JOIN_LOBBY" && _currentScreen.value != "host" -> {
                            // Spieler ist einer Lobby beigetreten
                            navigateTo("waiting")
                        }
                    }
                }
            } else if (res.startsWith("Error:")) {
                _errorMessage.value = res
            }
        } catch (e: Exception) {
            Log.e("AppViewModel", "Failed to parse JSON", e)
            _errorMessage.value = "Fehler bei Server-Kommunikation"
        }
    }
}
