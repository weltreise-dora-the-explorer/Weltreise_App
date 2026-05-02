package at.aau.serg.websocketbrokerdemo

import MyStomp
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import at.aau.serg.websocketbrokerdemo.models.City
import at.aau.serg.websocketbrokerdemo.models.Continent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
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
            "Epic Voyage" -> 12
            else -> 9
        }
        stomp.startGameCmd(_lobbyId.value, stops)
    }

    fun onRollDice() {
        stomp.rollDice(_lobbyId.value, _playerName.value)
    }

    fun onEndTurn() {
        val dice = _diceValue.value ?: return
        stomp.endTurn(_lobbyId.value, _playerName.value, dice)
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

                // Erfolgreiche Response
                if (rootJson.has("state") && !rootJson.isNull("state")) {
                    val stateJson = rootJson.getJSONObject("state")

                    // Spielerliste und Städte aktualisieren
                    if (stateJson.has("players")) {
                        val playersArray = stateJson.getJSONArray("players")
                        val newList = mutableListOf<String>()
                        val cityCountsMap = mutableMapOf<String, Int>()

                        for (i in 0 until playersArray.length()) {
                            val playerObj = playersArray.getJSONObject(i)
                            val pId = playerObj.getString("playerId")
                            newList.add(pId)

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
                        _playersList.value = newList
                        _playerCityCounts.value = cityCountsMap
                    }

                    // Würfelergebnis und aktueller Spieler (dein bestehender Code)
                    _diceValue.value = if (stateJson.isNull("lastDiceValue")) null else stateJson.optInt("lastDiceValue")
                    _currentTurnPlayerId.value = stateJson.optString("currentPlayerId").ifEmpty { null }

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
                        phase != "LOBBY" -> {
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
}
