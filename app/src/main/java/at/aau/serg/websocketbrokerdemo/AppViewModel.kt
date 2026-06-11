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

data class NewDestinationMessage(
    val playerName: String,
    val lostCityName: String,
    val newCityName: String
)

enum class ReportFeedback { HIT, MISS }

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

    // Letzter Spieler, der gewuerfelt hat. Bleibt ueber den Zugwechsel hinweg melde-bar,
    // bis ein anderer Spieler wuerfelt – passend zum Report-Fenster des Servers, das sich
    // ebenfalls erst beim naechsten Wurf schliesst (nicht beim Zugwechsel).
    private val _reportablePlayerId = MutableStateFlow<String?>(null)
    val reportablePlayerId: StateFlow<String?> = _reportablePlayerId.asStateFlow()

    private val _gamePhase = MutableStateFlow("LOBBY")
    val gamePhase: StateFlow<String> = _gamePhase.asStateFlow()

    private val _minigameWinnerPlayerId = MutableStateFlow<String?>(null)
    val minigameWinnerPlayerId: StateFlow<String?> = _minigameWinnerPlayerId.asStateFlow()

    private val _ownedCities = MutableStateFlow<List<City>>(emptyList())
    val ownedCities: StateFlow<List<City>> = _ownedCities.asStateFlow()

    private val _startCity = MutableStateFlow<City?>(null)
    val startCity: StateFlow<City?> = _startCity.asStateFlow()

    private val _playerCityCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val playerCityCounts: StateFlow<Map<String, Int>> = _playerCityCounts.asStateFlow()

    private val _playerReachedCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val playerReachedCounts: StateFlow<Map<String, Int>> = _playerReachedCounts.asStateFlow()

    private val _allPlayerOwnedCities = MutableStateFlow<Map<String, List<City>>>(emptyMap())
    val allPlayerOwnedCities: StateFlow<Map<String, List<City>>> = _allPlayerOwnedCities.asStateFlow()

    private val _allCities = MutableStateFlow<List<City>>(emptyList())
    val allCities: StateFlow<List<City>> = _allCities.asStateFlow()

    private val _playerCurrentCities = MutableStateFlow<Map<String, City?>>(emptyMap())
    val playerCurrentCities: StateFlow<Map<String, City?>> = _playerCurrentCities.asStateFlow()

    private val _optimisticPlayerCity = MutableStateFlow<City?>(null)
    val optimisticPlayerCity: StateFlow<City?> = _optimisticPlayerCity.asStateFlow()

    fun setOptimisticPlayerCity(city: City) {
        _optimisticPlayerCity.value = city
    }

    private val _validMoveIds = MutableStateFlow<List<String>>(emptyList())
    val validMoveIds: StateFlow<List<String>> = _validMoveIds.asStateFlow()

    private val _remainingSteps = MutableStateFlow<Int?>(null)
    val remainingSteps: StateFlow<Int?> = _remainingSteps.asStateFlow()

    private val _freePassCount = MutableStateFlow(0)
    val freePassCount: StateFlow<Int> = _freePassCount.asStateFlow()

    private val _playerStartCityNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val playerStartCityNames: StateFlow<Map<String, String>> = _playerStartCityNames.asStateFlow()

    private val _goalReachedMessage = MutableStateFlow<GoalReachedMessage?>(null)
    val goalReachedMessage: StateFlow<GoalReachedMessage?> = _goalReachedMessage.asStateFlow()

    private val _newDestinationMessage = MutableStateFlow<NewDestinationMessage?>(null)
    val newDestinationMessage: StateFlow<NewDestinationMessage?> = _newDestinationMessage.asStateFlow()

    private val _gameOverMessage = MutableStateFlow<GameOverMessage?>(null)
    val gameOverMessage: StateFlow<GameOverMessage?> = _gameOverMessage.asStateFlow()

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver.asStateFlow()

    // === Reconnect Recovery State ===
    private val _isReconnecting = MutableStateFlow(false)
    val isReconnecting: StateFlow<Boolean> = _isReconnecting.asStateFlow()

    private val _disconnectedPlayers = MutableStateFlow<Set<String>>(emptySet())
    val disconnectedPlayers: StateFlow<Set<String>> = _disconnectedPlayers.asStateFlow()

    private val _mustSkipPlayers = MutableStateFlow<Set<String>>(emptySet())
    val mustSkipPlayers: StateFlow<Set<String>> = _mustSkipPlayers.asStateFlow()

    private val _lastReportFeedback = MutableStateFlow<ReportFeedback?>(null)
    val lastReportFeedback: StateFlow<ReportFeedback?> = _lastReportFeedback.asStateFlow()

    // Kurzlebiger "skip turn"-Hinweis fuer eine Falschmeldung im eigenen Zug: dort verlieren
    // wir sofort den laufenden Zug, ohne dass der Server ein mustSkipNextTurn-Flag setzt – das
    // persistente Label haette also keine Datenquelle. Dieser Wert blendet das Label kurz ein.
    private val _transientSkipPlayerId = MutableStateFlow<String?>(null)
    val transientSkipPlayerId: StateFlow<String?> = _transientSkipPlayerId.asStateFlow()
    private var transientSkipJob: Job? = null

    private var pendingReportTarget: String? = null

    private val _secondsUntilRemoval = MutableStateFlow<Map<String, Int>>(emptyMap())
    val secondsUntilRemoval: StateFlow<Map<String, Int>> = _secondsUntilRemoval.asStateFlow()

    private val countdownJobs = mutableMapOf<String, Job>()
    private val gracePeriodSeconds: Int = 60

    private val _minigameLostCityName = MutableStateFlow<String?>(null)
    val minigameLostCityName: StateFlow<String?> = _minigameLostCityName.asStateFlow()

    private val _minigameNewCityName = MutableStateFlow<String?>(null)
    val minigameNewCityName: StateFlow<String?> = _minigameNewCityName.asStateFlow()

    private val _playerFreePassCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val playerFreePassCounts: StateFlow<Map<String, Int>> = _playerFreePassCounts.asStateFlow()

    private val _freePassReceivedEvent = MutableStateFlow(0)
    val freePassReceivedEvent: StateFlow<Int> = _freePassReceivedEvent.asStateFlow()

    private val _minigameSubPhase = MutableStateFlow<String?>(null)
    val minigameSubPhase: StateFlow<String?> = _minigameSubPhase.asStateFlow()

    private val _selectedMinigame = MutableStateFlow<String?>(null)
    val selectedMinigame: StateFlow<String?> = _selectedMinigame.asStateFlow()

    private val _guessQuestionText = MutableStateFlow<String?>(null)
    val guessQuestionText: StateFlow<String?> = _guessQuestionText.asStateFlow()

    private val _guessQuestionAnswer = MutableStateFlow<Int?>(null)
    val guessQuestionAnswer: StateFlow<Int?> = _guessQuestionAnswer.asStateFlow()

    private val _guessTimerEndMillis = MutableStateFlow<Long?>(null)
    val guessTimerEndMillis: StateFlow<Long?> = _guessTimerEndMillis.asStateFlow()

    private val _guessTimerDurationSeconds = MutableStateFlow<Int?>(null)
    val guessTimerDurationSeconds: StateFlow<Int?> = _guessTimerDurationSeconds.asStateFlow()

    private val _guessSubmissions = MutableStateFlow<Map<String, Int>>(emptyMap())
    val guessSubmissions: StateFlow<Map<String, Int>> = _guessSubmissions.asStateFlow()

    private val _guessSubmissionTimes = MutableStateFlow<Map<String, Long>>(emptyMap())
    val guessSubmissionTimes: StateFlow<Map<String, Long>> = _guessSubmissionTimes.asStateFlow()

    private val _myGuessSubmitted = MutableStateFlow(false)
    val myGuessSubmitted: StateFlow<Boolean> = _myGuessSubmitted.asStateFlow()

    // FLAG_GAME state (broadcast from the server)
    private val _flagRoundIndex = MutableStateFlow(0)
    val flagRoundIndex: StateFlow<Int> = _flagRoundIndex.asStateFlow()

    private val _flagCode = MutableStateFlow<String?>(null)
    val flagCode: StateFlow<String?> = _flagCode.asStateFlow()

    private val _flagOptions = MutableStateFlow<List<String>>(emptyList())
    val flagOptions: StateFlow<List<String>> = _flagOptions.asStateFlow()

    private val _flagCorrectName = MutableStateFlow<String?>(null)
    val flagCorrectName: StateFlow<String?> = _flagCorrectName.asStateFlow()

    private val _flagScores = MutableStateFlow<Map<String, Int>>(emptyMap())
    val flagScores: StateFlow<Map<String, Int>> = _flagScores.asStateFlow()

    private val _flagTotalTimeMs = MutableStateFlow<Map<String, Long>>(emptyMap())
    val flagTotalTimeMs: StateFlow<Map<String, Long>> = _flagTotalTimeMs.asStateFlow()

    private fun resetFlagClientState() {
        _flagRoundIndex.value = 0
        _flagCode.value = null
        _flagOptions.value = emptyList()
        _flagCorrectName.value = null
        _flagScores.value = emptyMap()
        _flagTotalTimeMs.value = emptyMap()
    }

    private var minigameStartSent = false

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
        stomp.startGameCmd(_lobbyId.value, _playerName.value, stops)
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

    fun finishMinigame(winnerPlayerId: String) {
        stomp.finishMinigame(
            lobbyId = _lobbyId.value,
            playerId = _playerName.value,
            winnerPlayerId = winnerPlayerId
        )
    }

    fun useFreePass() {
        if(_freePassCount.value <= 0) {
            return
        }

        stomp.useFreePass(
            lobbyId = _lobbyId.value,
            playerId = _playerName.value
        )
    }

    fun onShakeCheat() {
        Log.d("ShakeCheat", "phase=${_gamePhase.value} player=${_currentTurnPlayerId.value} me=${_playerName.value} steps=${_remainingSteps.value}")
        if (_gamePhase.value != "IN_TURN") return
        if (_currentTurnPlayerId.value != _playerName.value) return
        if (_remainingSteps.value != 1) return
        stomp.useShakeCheat(_lobbyId.value, _playerName.value)
    }

    fun reportCheat(reportedPlayerId: String) {
        if (_gamePhase.value == "LOBBY") return
        if (reportedPlayerId.isBlank()) return
        if (reportedPlayerId == _playerName.value) return
        if (reportedPlayerId !in _playersList.value) return
        pendingReportTarget = reportedPlayerId
        stomp.reportCheat(_lobbyId.value, _playerName.value, reportedPlayerId)
    }

    fun consumeReportFeedback() {
        _lastReportFeedback.value = null
    }

    private fun showTransientSelfSkip(playerId: String) {
        if (playerId.isBlank()) return
        transientSkipJob?.cancel()
        _transientSkipPlayerId.value = playerId
        transientSkipJob = viewModelScope.launch {
            delay(2500L)
            _transientSkipPlayerId.value = null
        }
    }

    fun startMinigame(force: Boolean = false) {
        if (minigameStartSent && !force) return
        minigameStartSent = true
        stomp.startMinigame(
            lobbyId = _lobbyId.value,
            playerId = _playerName.value
        )
    }

    fun submitGuess(guess: Int) {
        _myGuessSubmitted.value = true
        stomp.submitGuess(_lobbyId.value, _playerName.value, guess)
    }

    fun announceMinigameResult(winnerPlayerId: String) {
        stomp.announceMinigameResult(
            lobbyId = _lobbyId.value,
            playerId = _playerName.value,
            winnerPlayerId = winnerPlayerId
        )
    }

    fun playAgain() {
        if (_isHost.value) {
            _isGameOver.value = false
            _gameOverMessage.value = null
            _goalReachedMessage.value = null
            _newDestinationMessage.value = null
            _gamePhase.value = "LOBBY"
            _ownedCities.value = emptyList()
            _startCity.value = null
            _playerCityCounts.value = emptyMap()
            _playerReachedCounts.value = emptyMap()
            _playerCurrentCities.value = emptyMap()
            _diceValue.value = null
            _currentTurnPlayerId.value = null
            _reportablePlayerId.value = null
            transientSkipJob?.cancel()
            _transientSkipPlayerId.value = null
            _validMoveIds.value = emptyList()
            _remainingSteps.value = null
            _freePassCount.value = 0
            stomp.resetLobby(_lobbyId.value, _playerName.value)
        } else {
            // Nicht-Host: nur GameOver-Anzeige schliessen und in den Waiting-Screen wechseln.
            // Sobald der Host RESET_LOBBY ausloest, kommt ein frischer State per Broadcast.
            // Falls der Host stattdessen die Lobby schliesst, navigiert LOBBY_CLOSED uns zum Login.
            _isGameOver.value = false
            _gameOverMessage.value = null
            _goalReachedMessage.value = null
            navigateTo("waiting")
        }
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
        _gamePhase.value = "LOBBY"
        _isGameOver.value = false
        _goalReachedMessage.value = null
        _newDestinationMessage.value = null
        _gameOverMessage.value = null
        _playerStartCityNames.value = emptyMap()
        _ownedCities.value = emptyList()
        _startCity.value = null
        _playerCityCounts.value = emptyMap()
        _playerReachedCounts.value = emptyMap()
        _allPlayerOwnedCities.value = emptyMap()
        _playerCurrentCities.value = emptyMap()
        _diceValue.value = null
        _currentTurnPlayerId.value = null
        _reportablePlayerId.value = null
        transientSkipJob?.cancel()
        _transientSkipPlayerId.value = null
        _validMoveIds.value = emptyList()
        _remainingSteps.value = null
        _freePassCount.value = 0
        _mustSkipPlayers.value = emptySet()
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
        val safePrefs = prefs ?: return
        val storedLobbyId = safePrefs.getLobbyId() ?: return
        val storedPlayer = safePrefs.getPlayerName() ?: return
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
                    val failedCommandType = rootJson.optString("commandType")

                    // Versteckter Shake-Cheat: Fehler nicht im UI anzeigen,
                    // damit Mitspieler / der Spieler selbst nichts vom Versuch sieht.
                    if (failedCommandType == GameConstants.COMMAND_USE_SHAKE_CHEAT) {
                        Log.d("AppViewModel", "Shake-Cheat abgelehnt: $errorMsg")
                        return
                    }

                    // Report-Cheat-Fehler: pendingReportTarget aufraeumen, sonst
                    // koennte ein spaeterer fremder Report fälschlich als unser Feedback gewertet werden.
                    if (failedCommandType == GameConstants.COMMAND_REPORT_CHEAT) {
                        pendingReportTarget = null
                    }

                    _errorMessage.value = errorMsg
                    Log.e("CityTap", "Server-Fehler nach MOVE_TO_CITY: $errorMsg")
                    Log.e("AppViewModel", "Server-Fehler: $errorMsg")

                    // REJOIN fehlgeschlagen (z.B. nach Grace Period Timeout)
                    // → lobbyId aus Prefs loeschen und zurueck zum Login
                    if (failedCommandType == "REJOIN_LOBBY") {
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
                        val reachedCountsMap = mutableMapOf<String, Int>()
                        val allOwnedMap = _allPlayerOwnedCities.value.toMutableMap()
                        val currentCitiesMap = mutableMapOf<String, City?>()
                        val freePassCountsMap = mutableMapOf<String, Int>()
                        val startCityNamesMap = _playerStartCityNames.value.toMutableMap()
                        val disconnectedNow = mutableSetOf<String>()
                        val mustSkipNow = mutableSetOf<String>()

                        for (i in 0 until playersArray.length()) {
                            val playerObj = playersArray.getJSONObject(i)
                            val pId = playerObj.getString("playerId")
                            newList.add(pId)
                            freePassCountsMap[pId] = playerObj.optInt("freePassCount", 0)
                            if(pId == _playerName.value){
                                val newCount = playerObj.optInt("freePassCount", 0)
                                if (newCount > _freePassCount.value) {
                                    _freePassReceivedEvent.value += 1
                                }
                                _freePassCount.value = newCount
                            }
                            if (pId == stateJson.optString("currentPlayerId")) {
                                val playerRs = playerObj.optInt("remainingSteps", -1)
                                if (playerRs >= 0) _remainingSteps.value = playerRs
                            }

                            if (playerObj.has("connected") && !playerObj.getBoolean("connected")) {
                                disconnectedNow.add(pId)
                            }

                            if (playerObj.optBoolean("mustSkipNextTurn", false)) {
                                mustSkipNow.add(pId)
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
                                val visitedCount = if (playerObj.has("visitedCities")) playerObj.getJSONArray("visitedCities").length() else 0
                                reachedCountsMap[pId] = visitedCount

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

                                allOwnedMap[pId] = cities

                                if (pId == _playerName.value) {
                                    val oldOwnedCities = _ownedCities.value
                                    _ownedCities.value = cities

                                    val addedCity = cities.firstOrNull { newCity ->
                                        oldOwnedCities.none { oldCity -> oldCity.id == newCity.id }
                                    }

                                    val removedCity = oldOwnedCities.firstOrNull { oldCity ->
                                        cities.none { newCity -> newCity.id == oldCity.id }
                                    }

                                    if (addedCity != null && removedCity != null) {
                                        _newDestinationMessage.value = NewDestinationMessage(
                                            playerName = pId,
                                            lostCityName = removedCity.name,
                                            newCityName = addedCity.name
                                        )
                                    }

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
                        _playerReachedCounts.value = reachedCountsMap
                        _allPlayerOwnedCities.value = allOwnedMap
                        _playerCurrentCities.value = currentCitiesMap
                        _playerFreePassCounts.value = freePassCountsMap
                        _optimisticPlayerCity.value = null
                        _mustSkipPlayers.value = mustSkipNow
                        applyConnectionStatus(disconnectedNow)
                    }

                    // Würfelergebnis und aktueller Spieler (dein bestehender Code)
                    _diceValue.value = if (stateJson.isNull("lastDiceValue")) null else stateJson.optInt("lastDiceValue")
                    val newCurrentPlayerId = stateJson.optString("currentPlayerId").ifEmpty { null }
                    val isTurnChange = newCurrentPlayerId != _currentTurnPlayerId.value
                    _currentTurnPlayerId.value = newCurrentPlayerId
                    if (isTurnChange) _remainingSteps.value = null

                    // Melde-bar bleibt der letzte Wuerfler: nur bei einem tatsaechlichen Wurf
                    // (diceValue != null) wechselt das Ziel auf den aktuellen Spieler. Bei reinem
                    // Zugwechsel (diceValue == null, naechster Spieler noch nicht gewuerfelt) bleibt
                    // der bisherige Wuerfler melde-bar.
                    if (_diceValue.value != null && newCurrentPlayerId != null) {
                        _reportablePlayerId.value = newCurrentPlayerId
                    }

                    val validIds = mutableListOf<String>()
                    if (!isTurnChange && newCurrentPlayerId == _playerName.value) {
                        val validArray = stateJson.optJSONArray("validMoveIds")
                        if (validArray != null) {
                            for (i in 0 until validArray.length()) validIds.add(validArray.getString(i))
                        }
                    }
                    _validMoveIds.value = validIds

                    if (stateJson.has("remainingSteps") && !stateJson.isNull("remainingSteps")) {
                        _remainingSteps.value = stateJson.getInt("remainingSteps")
                    }

                    // hostId aus dem State lesen — fuer Auto-Rejoin (wir wissen sonst nicht
                    // ob der zurueckkehrende Spieler Host war).
                    val hostId = stateJson.optString("hostId", "")
                    if (hostId.isNotBlank() && hostId == _playerName.value) {
                        _isHost.value = true
                    }

                    // Navigation (dein bestehender Code)
                    val commandType = rootJson.optString("commandType", "")
                    val prevPhase = _gamePhase.value
                    val phase = stateJson.optString("phase", "LOBBY")
                    _gamePhase.value = phase

                    _minigameWinnerPlayerId.value =
                        if (stateJson.isNull("minigameWinnerPlayerId")) null
                        else stateJson.optString("minigameWinnerPlayerId").ifBlank { null }


                    // Parse minigame sub-phase and guess-game fields
                    val subPhase = if (stateJson.isNull("minigameSubPhase")) null
                        else stateJson.optString("minigameSubPhase").ifBlank { null }

                    if (prevPhase != GameConstants.PHASE_MINIGAME && phase == GameConstants.PHASE_MINIGAME) {
                        _myGuessSubmitted.value = false
                        _minigameSubPhase.value = null
                        _selectedMinigame.value = null
                        _guessQuestionText.value = null
                        _guessQuestionAnswer.value = null
                        _guessTimerEndMillis.value = null
                        _guessTimerDurationSeconds.value = null
                        _guessSubmissions.value = emptyMap()
                        _guessSubmissionTimes.value = emptyMap()
                        resetFlagClientState()
                    }
                    if (prevPhase == GameConstants.PHASE_MINIGAME && phase != GameConstants.PHASE_MINIGAME) {
                        minigameStartSent = false
                        _myGuessSubmitted.value = false
                        _minigameSubPhase.value = null
                        _selectedMinigame.value = null
                        _guessQuestionText.value = null
                        _guessQuestionAnswer.value = null
                        _guessTimerEndMillis.value = null
                        _guessTimerDurationSeconds.value = null
                        _guessSubmissions.value = emptyMap()
                        _guessSubmissionTimes.value = emptyMap()
                        resetFlagClientState()
                    }
                    if (subPhase == "SELECTING") {
                        minigameStartSent = false
                        _myGuessSubmitted.value = false
                        _guessQuestionText.value = null
                        _guessQuestionAnswer.value = null
                        _guessTimerEndMillis.value = null
                        _guessTimerDurationSeconds.value = null
                        _guessSubmissions.value = emptyMap()
                        _guessSubmissionTimes.value = emptyMap()
                        resetFlagClientState()
                    }

                    _minigameSubPhase.value = subPhase

                    _selectedMinigame.value = if (stateJson.isNull("selectedMinigame")) null
                        else stateJson.optString("selectedMinigame").ifBlank { null }

                    _guessQuestionText.value = if (stateJson.isNull("guessQuestionText")) null
                        else stateJson.optString("guessQuestionText").ifBlank { null }

                    _guessQuestionAnswer.value = if (stateJson.isNull("guessQuestionAnswer")) null
                        else stateJson.optInt("guessQuestionAnswer")

                    _guessTimerEndMillis.value = if (stateJson.isNull("guessTimerEndMillis")) null
                        else stateJson.optLong("guessTimerEndMillis").takeIf { it > 0L }

                    _guessTimerDurationSeconds.value = if (stateJson.isNull("timerDurationSeconds")) null
                        else stateJson.optInt("timerDurationSeconds").takeIf { it > 0 }

                    val submissionsJson = stateJson.optJSONObject("guessSubmissions")
                    if (submissionsJson != null) {
                        val submissions = mutableMapOf<String, Int>()
                        submissionsJson.keys().forEach { key -> submissions[key] = submissionsJson.optInt(key) }
                        _guessSubmissions.value = submissions
                        val times = _guessSubmissionTimes.value.toMutableMap()
                        val now = System.currentTimeMillis()
                        submissions.keys.forEach { key -> if (key !in times) times[key] = now }
                        _guessSubmissionTimes.value = times
                    } else {
                        _guessSubmissions.value = emptyMap()
                    }

                    // FLAG_GAME fields
                    _flagRoundIndex.value = if (stateJson.isNull("flagRoundIndex")) 0
                        else stateJson.optInt("flagRoundIndex")

                    _flagCode.value = if (stateJson.isNull("flagCode")) null
                        else stateJson.optString("flagCode").ifBlank { null }

                    _flagCorrectName.value = if (stateJson.isNull("flagCorrectName")) null
                        else stateJson.optString("flagCorrectName").ifBlank { null }

                    val flagOptionsJson = stateJson.optJSONArray("flagOptions")
                    _flagOptions.value = if (flagOptionsJson != null) {
                        (0 until flagOptionsJson.length()).map { flagOptionsJson.optString(it) }
                    } else emptyList()

                    val flagScoresJson = stateJson.optJSONObject("flagScores")
                    if (flagScoresJson != null) {
                        val scores = mutableMapOf<String, Int>()
                        flagScoresJson.keys().forEach { key -> scores[key] = flagScoresJson.optInt(key) }
                        _flagScores.value = scores
                    } else {
                        _flagScores.value = emptyMap()
                    }

                    val flagTimesJson = stateJson.optJSONObject("flagTotalTimeMs")
                    if (flagTimesJson != null) {
                        val times = mutableMapOf<String, Long>()
                        flagTimesJson.keys().forEach { key -> times[key] = flagTimesJson.optLong(key) }
                        _flagTotalTimeMs.value = times
                    } else {
                        _flagTotalTimeMs.value = emptyMap()
                    }

                    // Auto-trigger startMinigame for the current turn player on first MINIGAME entry.
                    // Skip if player has a free pass — they must choose via the dialog first.
                    if (phase == GameConstants.PHASE_MINIGAME && subPhase == null && !minigameStartSent) {
                        if (newCurrentPlayerId == _playerName.value && _freePassCount.value == 0) {
                            startMinigame()
                        }
                    }

                    if (commandType == GameConstants.COMMAND_REPORT_CHEAT) {
                        val target = pendingReportTarget
                        if (target != null) {
                            // Ein angenommener Report ist ein Treffer, wenn der Gemeldete jetzt
                            // aussetzen muss – sonst war es eine Falschmeldung. Nicht am eigenen
                            // mustSkip-Flag festmachen: bei einer Falschmeldung waehrend des eigenen
                            // Zugs verlieren wir stattdessen sofort den laufenden Zug (kein Flag).
                            val skipSet = _mustSkipPlayers.value
                            val feedback = if (target in skipSet) ReportFeedback.HIT else ReportFeedback.MISS
                            _lastReportFeedback.value = feedback
                            // MISS, aber wir tragen kein mustSkip-Flag -> der Server hat unseren
                            // laufenden Zug ausgesetzt (Falschmeldung im eigenen Zug). Das
                            // persistente "skip turn"-Label hat hier keine Quelle, also kurz
                            // selbst einblenden.
                            if (feedback == ReportFeedback.MISS && _playerName.value !in skipSet) {
                                showTransientSelfSkip(_playerName.value)
                            }
                            pendingReportTarget = null
                        }
                    }

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
                        commandType == "LEAVE_LOBBY" && phase == "LOBBY" && _currentScreen.value == "game" -> {
                            // Spiel wurde abgebrochen weil nur 1 Spieler uebrig ist
                            // -> zurueck zur Lobby (Host- oder Wartezimmer)
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

    override fun onMinigameLost(res: String) {
        try {
            val json = JSONObject(res)
            _minigameLostCityName.value = json.optString("lostCityName").ifBlank { null }
            _minigameNewCityName.value = json.optString("newCityName").ifBlank { null }
        } catch (e: Exception) {
            Log.e("AppViewModel", "Failed to parse minigame-lost", e)
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
            val winnerId = json.optString("winnerId", "")
            val array = json.getJSONArray("scores")
            val results = mutableListOf<GameOverMessage.PlayerResult>()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                results.add(GameOverMessage.PlayerResult(
                    playerName = item.optString("playerName"),
                    score = item.optInt("score")
                ))
            }
            _gameOverMessage.value = GameOverMessage(winnerId, results)
            navigateTo("gameover")
        } catch (e: Exception) {
            Log.e("AppViewModel", "Failed to parse game-over", e)
        }
    }
}
