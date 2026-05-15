package at.aau.serg.websocketbrokerdemo

import MyStomp
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.websocketbrokerdemo.models.City
import at.aau.serg.websocketbrokerdemo.models.Continent
import at.aau.serg.websocketbrokerdemo.models.GameOverMessage
import at.aau.serg.websocketbrokerdemo.models.GoalReachedMessage
import at.aau.serg.websocketbrokerdemo.preferences.PreferencesHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

open class AppViewModel(
    stompInstance: MyStomp? = null,
    private val prefs: PreferencesHelper? = null
) : ViewModel(), Callbacks {
    open val stomp: MyStomp = stompInstance ?: MyStomp(this)

    /**
     * Persistente clientId fuer dieses Geraet (UUID).
     * Wird nur erzeugt wenn PreferencesHelper vorhanden ist (in Tests = leer).
     */
    val clientId: String by lazy { prefs?.getOrCreateClientId() ?: "" }

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

    private val _diceValue = MutableStateFlow<Int?>(null)
    val diceValue: StateFlow<Int?> = _diceValue.asStateFlow()

    private val _currentTurnPlayerId = MutableStateFlow<String?>(null)
    val currentTurnPlayerId: StateFlow<String?> = _currentTurnPlayerId.asStateFlow()

    private val _ownedCities = MutableStateFlow<List<City>>(emptyList())
    val ownedCities: StateFlow<List<City>> = _ownedCities.asStateFlow()

    private val _startCity = MutableStateFlow<City?>(null)
    val startCity: StateFlow<City?> = _startCity.asStateFlow()

    private val _playerCityCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val playerCityCounts: StateFlow<Map<String, Int>> = _playerCityCounts.asStateFlow()

    private val _allCities = MutableStateFlow<List<City>>(emptyList())
    val allCities: StateFlow<List<City>> = _allCities.asStateFlow()

    private val _playerCurrentCities = MutableStateFlow<Map<String, City?>>(emptyMap())
    val playerCurrentCities: StateFlow<Map<String, City?>> = _playerCurrentCities.asStateFlow()

    private val _validMoveIds = MutableStateFlow<List<String>>(emptyList())
    val validMoveIds: StateFlow<List<String>> = _validMoveIds.asStateFlow()

    private val _remainingSteps = MutableStateFlow<Int?>(null)
    val remainingSteps: StateFlow<Int?> = _remainingSteps.asStateFlow()

    private val _playerStartCityNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val playerStartCityNames: StateFlow<Map<String, String>> = _playerStartCityNames.asStateFlow()

    private val _goalReachedMessage = MutableStateFlow<GoalReachedMessage?>(null)
    val goalReachedMessage: StateFlow<GoalReachedMessage?> = _goalReachedMessage.asStateFlow()

    private val _gameOverMessage = MutableStateFlow<GameOverMessage?>(null)
    val gameOverMessage: StateFlow<GameOverMessage?> = _gameOverMessage.asStateFlow()

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver.asStateFlow()

    // === Reconnect Recovery State ===
    private val _isReconnecting = MutableStateFlow(false)
    val isReconnecting: StateFlow<Boolean> = _isReconnecting.asStateFlow()

    private val _disconnectedPlayers = MutableStateFlow<Set<String>>(emptySet())
    val disconnectedPlayers: StateFlow<Set<String>> = _disconnectedPlayers.asStateFlow()

    private val _secondsUntilRemoval = MutableStateFlow<Map<String, Int>>(emptyMap())
    val secondsUntilRemoval: StateFlow<Map<String, Int>> = _secondsUntilRemoval.asStateFlow()

    private val countdownJobs = mutableMapOf<String, Job>()
    private val gracePeriodSeconds: Int = 60

    fun loadAllCities(context: Context) {
        try {
            val json = context.assets.open("cities.json").bufferedReader().readText()
            val array = JSONArray(json)
            val cities = mutableListOf<City>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val continent = try {
                    Continent.valueOf(obj.optString("continent", "EUROPE_AFRICA"))
                } catch (_: IllegalArgumentException) {
                    Continent.EUROPE_AFRICA
                }
                val trainConns = mutableListOf<String>()
                val trainArray = obj.optJSONArray("trainConnections")
                if (trainArray != null) {
                    for (j in 0 until trainArray.length()) trainConns.add(trainArray.getString(j))
                }
                val flightConns = mutableListOf<String>()
                val flightArray = obj.optJSONArray("flightConnections")
                if (flightArray != null) {
                    for (j in 0 until flightArray.length()) flightConns.add(flightArray.getString(j))
                }
                cities.add(City(
                    id = obj.optString("id", ""),
                    name = obj.optString("name", ""),
                    continent = continent,
                    color = obj.optString("color", ""),
                    x_relativ = obj.optDouble("x_relativ", 0.0).toFloat(),
                    y_relativ = obj.optDouble("y_relativ", 0.0).toFloat(),
                    trainConnections = trainConns,
                    flightConnections = flightConns
                ))
            }
            _allCities.value = cities
            Log.d("AppViewModel", "Alle Städte geladen: ${cities.size}")
        } catch (e: Exception) {
            Log.e("AppViewModel", "Fehler beim Laden der Städte", e)
        }
    }

    fun setGameMode(mode: String) {
        _gameMode.value = mode

        if(_isHost.value && _lobbyId.value.isNotBlank()){
            stomp.updateGameMode(_lobbyId.value, _playerName.value, mode)
        }
    }

    init {
        // Restore vom Prefs, falls die App neu gestartet wurde nachdem der Spieler
        // in einer Lobby war (Crash, manuelles Schliessen, OS-Kill).
        val storedName = prefs?.getPlayerName()
        if (!storedName.isNullOrBlank()) {
            _playerName.value = storedName
        }
        val storedLobby = prefs?.getLobbyId()
        if (!storedLobby.isNullOrBlank()) {
            _lobbyId.value = storedLobby
        }

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
        prefs?.setPlayerName(name)
    }

    fun joinLobby(pin: String) {
        _lobbyId.value = pin
        _isHost.value = false
        _isLoading.value = true
        _errorMessage.value = null
        prefs?.setLobbyId(pin)
        stomp.joinMultiplayerLobby(pin, _playerName.value, clientId.takeIf { it.isNotBlank() })
        // Navigation passiert jetzt in onResponse() nach Server-Bestätigung
    }

    fun hostLobby(name: String) {
        val randomPin = (1000..9999).random().toString()
        _lobbyId.value = randomPin
        _playerName.value = name
        _isHost.value = true
        _isLoading.value = true
        _errorMessage.value = null
        prefs?.setLobbyId(randomPin)
        prefs?.setPlayerName(name)
        stomp.createMultiplayerLobby(randomPin, name, clientId.takeIf { it.isNotBlank() })
        // Navigation passiert jetzt in onResponse() nach Server-Bestätigung
    }

    fun startGame() {
        val stops = when (_gameMode.value) {
            "City Hopper" -> 6
            "Epic Voyage" -> 12
            else -> 9
        }
        stomp.startGameCmd(_lobbyId.value, stops)
    }

    fun onRollDice() {
        stomp.rollDice(_lobbyId.value, _playerName.value)
    }

    fun onEndTurn() {
        if (_diceValue.value == null) return
        _validMoveIds.value = emptyList()
        _remainingSteps.value = null
        stomp.endTurn(_lobbyId.value, _playerName.value)
    }

    fun onMoveToCity(targetCityId: String) {
        Log.d("CityTap", "onMoveToCity: lobbyId='${_lobbyId.value}' player='${_playerName.value}' target='$targetCityId'")
        stomp.moveToCity(_lobbyId.value, _playerName.value, targetCityId)
    }

    fun playAgain() {
        _isGameOver.value = false
        _gameOverMessage.value = null
        _goalReachedMessage.value = null
        _ownedCities.value = emptyList()
        _startCity.value = null
        _playerCityCounts.value = emptyMap()
        _playerCurrentCities.value = emptyMap()
        _diceValue.value = null
        _currentTurnPlayerId.value = null
        _validMoveIds.value = emptyList()
        _remainingSteps.value = null
        stomp.resetLobby(_lobbyId.value, _playerName.value)
    }

    fun leaveLobby() {
        val currentLobbyId = _lobbyId.value
        val currentPlayerName = _playerName.value
        if (currentLobbyId.isNotBlank() && currentPlayerName.isNotBlank()) {
            stomp.leaveLobby(currentLobbyId, currentPlayerName)
        }
        prefs?.clearLobbyId()
        _lobbyId.value = ""
        _playersList.value = emptyList()
        _isHost.value = false
        _isGameOver.value = false
        _goalReachedMessage.value = null
        _gameOverMessage.value = null
        _playerStartCityNames.value = emptyMap()
        _ownedCities.value = emptyList()
        _startCity.value = null
        _playerCityCounts.value = emptyMap()
        _playerCurrentCities.value = emptyMap()
        _diceValue.value = null
        _currentTurnPlayerId.value = null
        _validMoveIds.value = emptyList()
        _remainingSteps.value = null
        clearDisconnectStates()
        navigateTo("login")
    }

    private fun clearDisconnectStates() {
        countdownJobs.values.forEach { it.cancel() }
        countdownJobs.clear()
        _disconnectedPlayers.value = emptySet()
        _secondsUntilRemoval.value = emptyMap()
        _isReconnecting.value = false
    }

    /**
     * Wird beim initialen Connect aufgerufen. Wenn in SharedPreferences eine
     * lobbyId + playerName gespeichert sind (Spieler war vor App-Kill in einem Spiel),
     * automatisch REJOIN_LOBBY senden.
     */
    private fun attemptAutoRejoinFromPrefs() {
        val storedLobbyId = prefs?.getLobbyId() ?: return
        val storedPlayer = prefs?.getPlayerName() ?: return
        if (storedLobbyId.isBlank() || storedPlayer.isBlank() || clientId.isBlank()) return

        _lobbyId.value = storedLobbyId
        _playerName.value = storedPlayer
        stomp.rejoinLobby(storedLobbyId, storedPlayer, clientId)
    }

    override fun onResponse(res: String) {
        Log.i("AppViewModel", "Received from server: $res")
        Log.d("CityTap", "onResponse: commandType=${runCatching { JSONObject(res).optString("commandType") }.getOrDefault("?")} validMoveIds=${runCatching { JSONObject(res).optJSONObject("state")?.optJSONArray("validMoveIds") }.getOrDefault("?")}")
        _isLoading.value = false

        // Initialer Connect erfolgreich → versuchen automatisch rejoinen falls Prefs Daten haben
        if (res == "connected") {
            attemptAutoRejoinFromPrefs()
            return
        }

        try {
            if (res.startsWith("{")) {
                val rootJson = JSONObject(res)

                // Prüfe success-Flag für Error-Handling
                if (rootJson.has("success") && !rootJson.getBoolean("success")) {
                    val errorMsg = rootJson.optString("message", "Unbekannter Fehler")
                    _errorMessage.value = errorMsg
                    Log.e("CityTap", "Server-Fehler nach MOVE_TO_CITY: $errorMsg")
                    Log.e("AppViewModel", "Server-Fehler: $errorMsg")

                    // REJOIN fehlgeschlagen (z.B. nach Grace Period Timeout)
                    // → lobbyId aus Prefs loeschen und zurueck zum Login
                    if (rootJson.optString("commandType") == "REJOIN_LOBBY") {
                        prefs?.clearLobbyId()
                        _lobbyId.value = ""
                        clearDisconnectStates()
                        navigateTo("login")
                    }
                    return
                }

                // Erfolgreiche Response
                if (rootJson.has("state") && !rootJson.isNull("state")) {
                    val stateJson = rootJson.getJSONObject("state")

                    if(stateJson.has("gameMode") && !stateJson.isNull("gameMode")){
                        _gameMode.value = stateJson.getString("gameMode")
                    }

                    // Spielerliste und Städte aktualisieren
                    if (stateJson.has("players")) {
                        val playersArray = stateJson.getJSONArray("players")
                        val newList = mutableListOf<String>()
                        val cityCountsMap = mutableMapOf<String, Int>()
                        val currentCitiesMap = mutableMapOf<String, City?>()
                        val startCityNamesMap = _playerStartCityNames.value.toMutableMap()
                        val disconnectedNow = mutableSetOf<String>()

                        for (i in 0 until playersArray.length()) {
                            val playerObj = playersArray.getJSONObject(i)
                            val pId = playerObj.getString("playerId")
                            newList.add(pId)
                            if (playerObj.has("connected") && !playerObj.getBoolean("connected")) {
                                disconnectedNow.add(pId)
                            }

                            if (playerObj.has("currentCity") && !playerObj.isNull("currentCity")) {
                                val cc = playerObj.getJSONObject("currentCity")
                                val ccContinent = try {
                                    Continent.valueOf(cc.optString("continent", "EUROPE_AFRICA"))
                                } catch (_: IllegalArgumentException) { Continent.EUROPE_AFRICA }
                                currentCitiesMap[pId] = City(
                                    id = cc.optString("id", ""),
                                    name = cc.optString("name", ""),
                                    continent = ccContinent,
                                    color = cc.optString("color", "")
                                )
                            } else {
                                currentCitiesMap[pId] = null
                            }

                            // Startstadt für alle Spieler merken (wird für "letzte Stadt"-Meldung gebraucht)
                            if (playerObj.has("startCity") && !playerObj.isNull("startCity")) {
                                val scName = playerObj.getJSONObject("startCity").optString("name", "")
                                if (scName.isNotBlank()) startCityNamesMap[pId] = scName
                            }

                            if (playerObj.has("ownedCities")) {
                                val citiesArray = playerObj.getJSONArray("ownedCities")
                                cityCountsMap[pId] = citiesArray.length()

                                if (pId == _playerName.value) {
                                    val cities = mutableListOf<City>()
                                    for (j in 0 until citiesArray.length()) {
                                        val cityObj = citiesArray.getJSONObject(j)
                                        val continent = try {
                                            Continent.valueOf(cityObj.optString("continent", "EUROPE"))
                                        } catch (_: IllegalArgumentException) {
                                            Continent.EUROPE_AFRICA
                                        }
                                        cities.add(City(
                                            id = cityObj.optString("id", ""),
                                            name = cityObj.optString("name", ""),
                                            continent = continent,
                                            color = cityObj.optString("color", "")
                                        ))
                                    }
                                    _ownedCities.value = cities
                                    Log.d("AppViewModel", "Eigene Städte empfangen: ${cities.map { it.name }}")

                                    if (playerObj.has("startCity") && !playerObj.isNull("startCity")) {
                                        val sc = playerObj.getJSONObject("startCity")
                                        val continent = try {
                                            Continent.valueOf(sc.optString("continent", "EUROPE"))
                                        } catch (_: IllegalArgumentException) {
                                            Continent.EUROPE_AFRICA
                                        }
                                        _startCity.value = City(
                                            id = sc.optString("id", ""),
                                            name = sc.optString("name", ""),
                                            continent = continent,
                                            color = sc.optString("color", "")
                                        )
                                    }
                                }
                            }
                        }
                        _playerStartCityNames.value = startCityNamesMap
                        _playersList.value = newList
                        _playerCityCounts.value = cityCountsMap
                        _playerCurrentCities.value = currentCitiesMap
                        applyConnectionStatus(disconnectedNow)
                    }

                    // Würfelergebnis und aktueller Spieler (dein bestehender Code)
                    _diceValue.value = if (stateJson.isNull("lastDiceValue")) null else stateJson.optInt("lastDiceValue")
                    val newCurrentPlayerId = stateJson.optString("currentPlayerId").ifEmpty { null }
                    val isTurnChange = newCurrentPlayerId != _currentTurnPlayerId.value
                    _currentTurnPlayerId.value = newCurrentPlayerId

                    val validIds = mutableListOf<String>()
                    if (!isTurnChange && newCurrentPlayerId == _playerName.value) {
                        val validArray = stateJson.optJSONArray("validMoveIds")
                        if (validArray != null) {
                            for (i in 0 until validArray.length()) validIds.add(validArray.getString(i))
                        }
                    }
                    _validMoveIds.value = validIds

                    val rs = stateJson.optInt("remainingSteps", -1)
                    _remainingSteps.value = if (rs >= 0) rs else null

                    // hostId aus dem State lesen — fuer Auto-Rejoin (wir wissen sonst nicht
                    // ob der zurueckkehrende Spieler Host war).
                    val hostId = stateJson.optString("hostId", "")
                    if (hostId.isNotBlank() && hostId == _playerName.value) {
                        _isHost.value = true
                    }

                    // Navigation (dein bestehender Code)
                    val commandType = rootJson.optString("commandType", "")
                    val phase = stateJson.optString("phase", "LOBBY")

                    when {
                        commandType == "LOBBY_CLOSED" -> {
                            _lobbyId.value = ""
                            _playersList.value = emptyList()
                            _isHost.value = false
                            navigateTo("login")
                        }
                        commandType == "RESET_LOBBY" -> {
                            if (_isHost.value) navigateTo("host")
                            else navigateTo("waiting")
                        }
                        commandType == "PLAYER_RECONNECTED" && phase == "LOBBY" -> {
                            if (_isHost.value) navigateTo("host")
                            else navigateTo("waiting")
                        }
                        phase != "LOBBY" && !_isGameOver.value -> {
                            navigateTo("game")
                        }
                        commandType == "CREATE_LOBBY" -> {
                            navigateTo("host")
                        }
                        commandType == "JOIN_LOBBY" && _currentScreen.value != "host" -> {
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

    override fun onGoalReached(res: String) {
        try {
            val json = JSONObject(res)
            _goalReachedMessage.value = GoalReachedMessage(
                playerName = json.optString("playerName"),
                cityName = json.optString("cityName"),
                reached = json.optInt("reached"),
                total = json.optInt("total")
            )
        } catch (e: Exception) {
            Log.e("AppViewModel", "Failed to parse goal-reached", e)
        }
    }

    override fun onConnectionLost() {
        _isReconnecting.value = true
    }

    override fun onReconnected() {
        _isReconnecting.value = false
        // Wenn lobbyId in Prefs gespeichert ist und wir nicht im Login-Screen sind:
        // automatisch REJOIN_LOBBY senden um zurueck ins Spiel zu kommen
        val storedLobbyId = prefs?.getLobbyId() ?: return
        val player = _playerName.value
        if (storedLobbyId.isNotBlank() && player.isNotBlank() && clientId.isNotBlank()) {
            stomp.rejoinLobby(storedLobbyId, player, clientId)
        }
    }

    private fun applyConnectionStatus(disconnectedNow: Set<String>) {
        val previouslyDisconnected = _disconnectedPlayers.value
        _disconnectedPlayers.value = disconnectedNow

        // Spieler die NEU disconnected sind: Countdown starten
        for (playerId in disconnectedNow - previouslyDisconnected) {
            startRemovalCountdown(playerId)
        }

        // Spieler die zurueck sind: Countdown abbrechen
        for (playerId in previouslyDisconnected - disconnectedNow) {
            cancelRemovalCountdown(playerId)
        }
    }

    private fun startRemovalCountdown(playerId: String) {
        cancelRemovalCountdown(playerId)
        _secondsUntilRemoval.value = _secondsUntilRemoval.value + (playerId to gracePeriodSeconds)
        countdownJobs[playerId] = viewModelScope.launch {
            var remaining = gracePeriodSeconds
            while (remaining > 0) {
                delay(1000L)
                remaining -= 1
                _secondsUntilRemoval.value = _secondsUntilRemoval.value + (playerId to remaining)
            }
            _secondsUntilRemoval.value = _secondsUntilRemoval.value - playerId
        }
    }

    private fun cancelRemovalCountdown(playerId: String) {
        countdownJobs.remove(playerId)?.cancel()
        _secondsUntilRemoval.value = _secondsUntilRemoval.value - playerId
    }

    override fun onGameOver(res: String) {
        _isGameOver.value = true
        try {
            val json = JSONObject(res)
            val array = json.getJSONArray("scores")
            val results = mutableListOf<GameOverMessage.PlayerResult>()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                results.add(GameOverMessage.PlayerResult(
                    playerName = item.optString("playerName"),
                    score = item.optInt("score")
                ))
            }
            _gameOverMessage.value = GameOverMessage(results)
            navigateTo("gameover")
        } catch (e: Exception) {
            Log.e("AppViewModel", "Failed to parse game-over", e)
        }
    }
}
