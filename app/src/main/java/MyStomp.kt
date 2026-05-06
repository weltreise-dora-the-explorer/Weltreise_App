import android.os.Handler
import android.os.Looper
import android.util.Log
import at.aau.serg.websocketbrokerdemo.Callbacks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import org.json.JSONObject

private const val WEBSOCKET_URI = "ws://10.0.2.2:8080/websocket-example-broker"

class MyStomp(val callbacks: Callbacks) {
    private var topicFlow: Flow<String>? = null
    private var collector: Job? = null
    private var jsonFlow: Flow<String>? = null
    private var jsonCollector: Job? = null

    private lateinit var client: StompClient
    private var session: StompSession? = null

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    fun connect() {
        client = StompClient(OkHttpWebSocketClient()) // other config can be passed in here
        scope.launch {
            try {
                val activeSession = client.connect(WEBSOCKET_URI)
                session = activeSession

                // connect to topic
                topicFlow = activeSession.subscribeText("/topic/hello-response")
                collector = scope.launch {
                    topicFlow?.collect { msg ->
                        // TODO logic
                        callback(msg)
                    }
                }

                // connect to JSON topic
                jsonFlow = activeSession.subscribeText("/topic/rcv-object")
                jsonCollector = scope.launch {
                    jsonFlow?.collect { msg ->
                        val o = JSONObject(msg)
                        callback(o.get("text").toString())
                    }
                }
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

    fun joinMultiplayerLobby(lobbyId: String, playerId: String) {
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
                val lobbyFlow = session?.subscribeText("/topic/lobby/$lobbyId/events")
                scope.launch {
                    lobbyFlow?.collect { msg ->
                        Log.i("Lobby-Update", "Vom Server gesynctes Spielfeld: $msg")
                        callback(msg) // Send to ViewModel
                    }
                }
                
                // 2. Warten, damit der Server das Subscribe sicher verarbeitet hat
                delay(500)

                // 3. Send JOIN_LOBBY command
                val joinCommand = JSONObject()
                joinCommand.put("type", "JOIN_LOBBY")
                joinCommand.put("playerId", playerId)
                
                session?.sendText("/app/lobby/$lobbyId/command", joinCommand.toString())
                Log.i("Lobby", "Join-Befehl an Server geschickt für Spieler: $playerId")
                
            } catch (e: Exception) {
                Log.e("MyStomp", "Fehler beim Lobby Join", e)
                callback("Error: Lobby Join Failed")
            }
        }
    }

    fun createMultiplayerLobby(lobbyId: String, playerId: String) {
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
                val lobbyFlow = session?.subscribeText("/topic/lobby/$lobbyId/events")
                scope.launch {
                    lobbyFlow?.collect { msg ->
                        Log.i("Lobby-Update", "Vom Server gesynctes Spielfeld: $msg")
                        callback(msg)
                    }
                }

                // 2. Warten, damit der Server das Subscribe sicher verarbeitet hat
                delay(500)

                // 3. Send CREATE_LOBBY command (nicht JOIN_LOBBY!)
                val createCommand = JSONObject()
                createCommand.put("type", "CREATE_LOBBY")
                createCommand.put("playerId", playerId)

                session?.sendText("/app/lobby/$lobbyId/command", createCommand.toString())
                Log.i("Lobby", "Create-Befehl an Server geschickt für Spieler: $playerId")

            } catch (e: Exception) {
                Log.e("MyStomp", "Fehler beim Lobby erstellen", e)
                callback("Error: Lobby Creation Failed")
            }
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

    fun endTurn(lobbyId: String, playerId: String, diceValue: Int) {
        scope.launch {
            try {
                val command = JSONObject()
                command.put("type", "MOVE_TOKEN")
                command.put("playerId", playerId)
                command.put("moveSteps", diceValue)
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