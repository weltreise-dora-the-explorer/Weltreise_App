import android.os.Handler
import android.os.Looper
import android.util.Log
import at.aau.serg.websocketbrokerdemo.Callbacks
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import org.json.JSONObject

//private const val WEBSOCKET_URI = "ws://10.0.2.2:8080/websocket-example-broker"
private const val WEBSOCKET_URI = "ws://se2-demo.aau.at:53205/websocket-example-broker"
private const val RECONNECT_INITIAL_DELAY_MS = 2000L
private const val RECONNECT_MAX_DELAY_MS = 30_000L

class MyStomp(val callbacks: Callbacks) {
    private var topicFlow: Flow<String>? = null
    private var collector: Job? = null
    private var jsonFlow: Flow<String>? = null
    private var jsonCollector: Job? = null

    private lateinit var client: StompClient
    private var session: StompSession? = null

    /**
     * Defensiver Coroutine-Scope:
     *  - SupervisorJob: ein abstuerzender Topic-Flow cancelt nicht alle anderen
     *  - CoroutineExceptionHandler: unhandled Exceptions werden nur geloggt,
     *    statt durchgereicht zu werden -> Android killt den Prozess nicht mehr.
     */
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("MyStomp", "Uncaught coroutine exception", throwable)
    }
    private val scope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    @Volatile
    private var reconnecting: Boolean = false

    fun isConnected(): Boolean = session != null

    fun connect() {
        client = StompClient(OkHttpWebSocketClient()) // other config can be passed in here
        scope.launch {
            try {
                val activeSession = client.connect(WEBSOCKET_URI)
                session = activeSession

                // connect to topic
                topicFlow = activeSession.subscribeText("/topic/hello-response")
                collector = scope.launch {
                    collectSafely("hello-response") {
                        topicFlow?.collect { msg ->
                            // TODO logic
                            callback(msg)
                        }
                    }
                }

                // connect to JSON topic
                jsonFlow = activeSession.subscribeText("/topic/rcv-object")
                jsonCollector = scope.launch {
                    collectSafely("rcv-object") {
                        jsonFlow?.collect { msg ->
                            val o = JSONObject(msg)
                            callback(o.get("text").toString())
                        }
                    }
                }

                val goalReachedFlow = activeSession.subscribeText("/topic/goal-reached")
                scope.launch {
                    collectSafely("goal-reached") {
                        goalReachedFlow.collect { msg ->
                            Log.d("MyStomp", "GOAL-REACHED received: $msg")
                            callbackGoalReached(msg)
                        }
                    }
                }
                Log.d("MyStomp", "Subscribed to /topic/goal-reached")

                val gameOverFlow = activeSession.subscribeText("/topic/game-over")
                scope.launch {
                    collectSafely("game-over") {
                        gameOverFlow.collect { msg ->
                            Log.d("MyStomp", "GAME-OVER received: $msg")
                            callbackGameOver(msg)
                        }
                    }
                }
                Log.d("MyStomp", "Subscribed to /topic/game-over")

                callback("connected")

            } catch (e: Exception) {
                Log.e("MyStomp", "Connection failed", e)
                callback("Connection error")
            }
        }
    }

    private fun callback(msg: String) {
        Handler(Looper.getMainLooper()).post {
            callbacks.onResponse(msg)
        }
    }

    private fun callbackGoalReached(msg: String) {
        Handler(Looper.getMainLooper()).post {
            callbacks.onGoalReached(msg)
        }
    }

    private fun callbackGameOver(msg: String) {
        Handler(Looper.getMainLooper()).post {
            callbacks.onGameOver(msg)
        }
    }

    fun joinMultiplayerLobby(lobbyId: String, playerId: String, clientId: String? = null) {
        scope.launch {
            try {
                // Warte, falls die App gerade erst gestartet ist und der Socket noch verbindet
                var attempts = 0
                while(session == null && attempts < 50) {
                    delay(100)
                    attempts++
                }

                if (session == null) {
                    Log.e("MyStomp", "ABBRUCH: Keine StompSession! Ist der Server an?")
                    return@launch
                }

                // 1. Subscribe to lobby events
                subscribeLobbyEvents(lobbyId)

                // 2. Warten, damit der Server das Subscribe sicher verarbeitet hat
                delay(500)

                // 3. Send JOIN_LOBBY command
                val joinCommand = JSONObject()
                joinCommand.put("type", "JOIN_LOBBY")
                joinCommand.put("playerId", playerId)
                if (!clientId.isNullOrBlank()) {
                    joinCommand.put("clientId", clientId)
                }

                session?.sendText("/app/lobby/$lobbyId/command", joinCommand.toString())
                Log.i("Lobby", "Join-Befehl an Server geschickt für Spieler: $playerId")

            } catch (e: Exception) {
                Log.e("MyStomp", "Fehler beim Lobby Join", e)
                callback("Error: Lobby Join Failed")
            }
        }
    }

    fun createMultiplayerLobby(lobbyId: String, playerId: String, clientId: String? = null) {
        scope.launch {
            try {
                // Warte auf Session falls nötig
                var attempts = 0
                while(session == null && attempts < 50) {
                    delay(100)
                    attempts++
                }

                if (session == null) {
                    Log.e("MyStomp", "ABBRUCH: Keine StompSession! Ist der Server an?")
                    callback("Error: Not connected")
                    return@launch
                }

                // 1. Subscribe to lobby events
                subscribeLobbyEvents(lobbyId)

                // 2. Warten, damit der Server das Subscribe sicher verarbeitet hat
                delay(500)

                // 3. Send CREATE_LOBBY command (nicht JOIN_LOBBY!)
                val createCommand = JSONObject()
                createCommand.put("type", "CREATE_LOBBY")
                createCommand.put("playerId", playerId)
                if (!clientId.isNullOrBlank()) {
                    createCommand.put("clientId", clientId)
                }

                session?.sendText("/app/lobby/$lobbyId/command", createCommand.toString())
                Log.i("Lobby", "Create-Befehl an Server geschickt für Spieler: $playerId")

            } catch (e: Exception) {
                Log.e("MyStomp", "Fehler beim Lobby erstellen", e)
                callback("Error: Lobby Creation Failed")
            }
        }
    }

    /**
     * Schickt einen REJOIN_LOBBY Befehl an den Server.
     * Wird nach einem Verbindungsabbruch verwendet, um zurueck in die laufende Lobby zu kommen.
     */
    fun rejoinLobby(lobbyId: String, playerId: String, clientId: String) {
        scope.launch {
            try {
                var attempts = 0
                while (session == null && attempts < 50) {
                    delay(100)
                    attempts++
                }
                if (session == null) {
                    Log.e("MyStomp", "ABBRUCH: rejoinLobby ohne Session.")
                    return@launch
                }

                subscribeLobbyEvents(lobbyId)
                delay(500)

                val rejoinCommand = JSONObject()
                rejoinCommand.put("type", "REJOIN_LOBBY")
                rejoinCommand.put("playerId", playerId)
                rejoinCommand.put("clientId", clientId)

                session?.sendText("/app/lobby/$lobbyId/command", rejoinCommand.toString())
                Log.i("MyStomp", "REJOIN_LOBBY sent for lobby=$lobbyId player=$playerId")
            } catch (e: Exception) {
                Log.e("MyStomp", "Fehler beim Rejoin", e)
                callback("Error: Lobby Rejoin Failed")
            }
        }
    }

    /**
     * Subscribed auf /topic/lobby/{lobbyId}/events. Bei Verbindungsabbruch wird
     * der `onConnectionLost`-Callback ausgeloest und ein Reconnect versucht.
     */
    private suspend fun subscribeLobbyEvents(lobbyId: String) {
        val lobbyFlow = session?.subscribeText("/topic/lobby/$lobbyId/events") ?: return
        scope.launch {
            try {
                lobbyFlow.collect { msg ->
                    Log.i("Lobby-Update", "Vom Server gesynctes Spielfeld: $msg")
                    callback(msg)
                }
            } catch (e: Exception) {
                Log.w("MyStomp", "Lobby events flow ended with exception: ${e.message}")
                handleConnectionLost()
            }
        }
    }

    /**
     * Sammelt einen Flow innerhalb eines try/catch. Faengt jeden Fehler ab
     * (z.B. Server-Stop -> WebSocket bricht weg) und triggert einen Reconnect,
     * statt die Exception bis zum UncaughtExceptionHandler propagieren zu lassen
     * (was zu einem Android-Prozesscrash und dem "App-Sturz auf Home-Screen" fuehrt).
     */
    private suspend fun collectSafely(topicName: String, block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            Log.w("MyStomp", "Topic $topicName ended with exception: ${e.message}")
            handleConnectionLost()
        }
    }

    private fun handleConnectionLost() {
        session = null
        Handler(Looper.getMainLooper()).post {
            callbacks.onConnectionLost()
        }
        scheduleReconnect()
    }

    private fun scheduleReconnect() {
        if (reconnecting) return
        reconnecting = true
        scope.launch {
            var delayMs = RECONNECT_INITIAL_DELAY_MS
            while (session == null) {
                try {
                    delay(delayMs)
                    Log.i("MyStomp", "Reconnect attempt...")
                    val activeSession = client.connect(WEBSOCKET_URI)
                    session = activeSession
                    Handler(Looper.getMainLooper()).post {
                        callbacks.onReconnected()
                    }
                    break
                } catch (e: Exception) {
                    Log.w("MyStomp", "Reconnect failed: ${e.message}")
                    delayMs = (delayMs * 2).coerceAtMost(RECONNECT_MAX_DELAY_MS)
                }
            }
            reconnecting = false
        }
    }

    fun startGameCmd(lobbyId: String, stops: Int) {
        scope.launch {
            try {
                val command = JSONObject()
                command.put("type", "START_GAME")
                command.put("stops", stops)
                session?.sendText("/app/lobby/$lobbyId/command", command.toString())
            } catch (e: Exception) {
                Log.e("MyStomp", "Fehler beim Spielstart", e)
            }
        }
    }

    fun updateGameMode(lobbyId: String, playerId: String, gameMode: String){
        scope.launch {
            try{
                val command = JSONObject()
                command.put("type", "UPDATE_GAME_MODE")
                command.put("playerId", playerId)
                command.put("gameMode", gameMode)

                session?.sendText("/app/lobby/$lobbyId/command", command.toString())
                Log.i("Lobby", "Game mode update sent: $gameMode by player: $playerId")
            } catch (e: Exception){
                Log.e("MyStomp", "Fehler beim Aktualisieren des Spielmodus", e)
            }
        }
    }

    fun resetLobby(lobbyId: String, playerId: String) {
        scope.launch {
            try {
                val command = JSONObject()
                command.put("type", "RESET_LOBBY")
                command.put("playerId", playerId)
                session?.sendText("/app/lobby/$lobbyId/command", command.toString())
                Log.i("MyStomp", "RESET_LOBBY sent for lobby: $lobbyId by: $playerId")
            } catch (e: Exception) {
                Log.e("MyStomp", "Fehler beim Reset der Lobby", e)
            }
        }
    }

    fun leaveLobby(lobbyId: String, playerId: String) {
        scope.launch {
            try {
                val command = JSONObject()
                command.put("type", "LEAVE_LOBBY")
                command.put("playerId", playerId)
                session?.sendText("/app/lobby/$lobbyId/command", command.toString())
            } catch (e: Exception) {
                Log.e("MyStomp", "Fehler beim Lobby verlassen", e)
            }
        }
    }

    fun rollDice(lobbyId: String, playerId: String) {
        scope.launch {
            try {
                val command = JSONObject()
                command.put("type", "ROLL_DICE")
                command.put("playerId", playerId)
                session?.sendText("/app/lobby/$lobbyId/command", command.toString())
            } catch (e: Exception) {
                Log.e("MyStomp", "Fehler beim Würfeln", e)
            }
        }
    }

    fun endTurn(lobbyId: String, playerId: String) {
        scope.launch {
            try {
                val command = JSONObject()
                command.put("type", "END_TURN")
                command.put("playerId", playerId)
                session?.sendText("/app/lobby/$lobbyId/command", command.toString())
            } catch (e: Exception) {
                Log.e("MyStomp", "Fehler beim Zug beenden", e)
            }
        }
    }

    fun moveToCity(lobbyId: String, playerId: String, targetCityId: String) {
        scope.launch {
            try {
                val dest = "/app/lobby/$lobbyId/command"
                Log.d("CityTap", "moveToCity: session=${session != null}, dest=$dest")
                val command = JSONObject()
                command.put("type", "MOVE_TO_CITY")
                command.put("playerId", playerId)
                command.put("targetCityId", targetCityId)
                if (session == null) {
                    Log.e("CityTap", "moveToCity ABGEBROCHEN: session ist null!")
                    return@launch
                }
                session!!.sendText(dest, command.toString())
                Log.d("CityTap", "moveToCity: gesendet → $command")
            } catch (e: Exception) {
                Log.e("CityTap", "moveToCity Exception: ${e.message}", e)
            }
        }
    }

}