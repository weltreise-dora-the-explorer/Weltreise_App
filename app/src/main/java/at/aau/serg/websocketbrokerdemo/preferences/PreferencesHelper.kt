package at.aau.serg.websocketbrokerdemo.preferences

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

/**
 * SharedPreferences-Wrapper für persistente App-Daten, die für Reconnect Recovery
 * benötigt werden. Speichert eine clientId (eindeutig pro Geräte/App-Installation)
 * und die aktuelle lobbyId.
 */
class PreferencesHelper(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Liefert die persistente clientId für dieses Gerät.
     * Wird beim ersten Aufruf einmalig erzeugt und in den SharedPreferences gespeichert.
     */
    fun getOrCreateClientId(): String {
        val existing = prefs.getString(KEY_CLIENT_ID, null)
        if (existing != null) return existing

        val newId = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_CLIENT_ID, newId).apply()
        return newId
    }

    /**
     * Liefert die zuletzt gespeicherte lobbyId, oder null wenn keine gesetzt ist.
     */
    fun getLobbyId(): String? = prefs.getString(KEY_LOBBY_ID, null)

    /**
     * Speichert die aktuelle lobbyId (für späteren Reconnect).
     */
    fun setLobbyId(lobbyId: String) {
        prefs.edit().putString(KEY_LOBBY_ID, lobbyId).apply()
    }

    /**
     * Loescht die gespeicherte lobbyId (z.B. nach Leave Lobby).
     */
    fun clearLobbyId() {
        prefs.edit().remove(KEY_LOBBY_ID).apply()
    }

    /**
     * Liefert den zuletzt verwendeten Spielernamen, oder null wenn keiner gesetzt.
     * Wird fuer Auto-Rejoin nach App-Neustart benoetigt.
     */
    fun getPlayerName(): String? = prefs.getString(KEY_PLAYER_NAME, null)

    fun setPlayerName(name: String) {
        prefs.edit().putString(KEY_PLAYER_NAME, name).apply()
    }

    fun clearPlayerName() {
        prefs.edit().remove(KEY_PLAYER_NAME).apply()
    }

    companion object {
        private const val PREFS_NAME = "weltreise_prefs"
        private const val KEY_CLIENT_ID = "client_id"
        private const val KEY_LOBBY_ID = "lobby_id"
        private const val KEY_PLAYER_NAME = "player_name"
    }
}
