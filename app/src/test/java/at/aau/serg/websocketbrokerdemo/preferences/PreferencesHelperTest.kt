package at.aau.serg.websocketbrokerdemo.preferences

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PreferencesHelperTest {

    private lateinit var context: Context
    private lateinit var appContext: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val storage = mutableMapOf<String, String?>()

    @BeforeEach
    fun setUp() {
        storage.clear()
        context = mockk(relaxed = true)
        appContext = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        editor = mockk(relaxed = true)

        every { context.applicationContext } returns appContext
        every { appContext.getSharedPreferences(any(), any()) } returns prefs

        // Lese-Verhalten: liefere aus internem Storage
        every { prefs.getString(any(), any()) } answers {
            val key = firstArg<String>()
            storage[key] ?: secondArg()
        }

        // Schreib-Verhalten: Edit-Slot
        every { prefs.edit() } returns editor
        val keySlot = slot<String>()
        val valueSlot = slot<String>()
        every { editor.putString(capture(keySlot), capture(valueSlot)) } answers {
            storage[keySlot.captured] = valueSlot.captured
            editor
        }
        val removeKeySlot = slot<String>()
        every { editor.remove(capture(removeKeySlot)) } answers {
            storage.remove(removeKeySlot.captured)
            editor
        }
        every { editor.apply() } answers { }
    }

    @Test
    fun `getOrCreateClientId returns the same UUID on subsequent calls`() {
        val helper = PreferencesHelper(context)

        val first = helper.getOrCreateClientId()
        val second = helper.getOrCreateClientId()

        assertNotNull(first)
        assertTrue(first.isNotBlank())
        assertEquals(first, second)
    }

    @Test
    fun `getOrCreateClientId persists the generated UUID`() {
        val helper = PreferencesHelper(context)

        val clientId = helper.getOrCreateClientId()

        verify { editor.putString("client_id", clientId) }
        verify { editor.apply() }
    }

    @Test
    fun `getOrCreateClientId returns previously stored UUID`() {
        storage["client_id"] = "existing-uuid-abc"
        val helper = PreferencesHelper(context)

        val clientId = helper.getOrCreateClientId()

        assertEquals("existing-uuid-abc", clientId)
    }

    @Test
    fun `getLobbyId returns null when nothing stored`() {
        val helper = PreferencesHelper(context)

        assertNull(helper.getLobbyId())
    }

    @Test
    fun `setLobbyId persists the value`() {
        val helper = PreferencesHelper(context)

        helper.setLobbyId("1234")

        assertEquals("1234", storage["lobby_id"])
        verify { editor.putString("lobby_id", "1234") }
    }

    @Test
    fun `getLobbyId returns the previously set value`() {
        val helper = PreferencesHelper(context)
        helper.setLobbyId("5678")

        assertEquals("5678", helper.getLobbyId())
    }

    @Test
    fun `clearLobbyId removes the stored value`() {
        val helper = PreferencesHelper(context)
        helper.setLobbyId("9999")

        helper.clearLobbyId()

        assertNull(helper.getLobbyId())
        verify { editor.remove("lobby_id") }
    }

    @Test
    fun `clientId and lobbyId persist independently`() {
        storage["client_id"] = "uuid-1"
        val helper = PreferencesHelper(context)

        helper.setLobbyId("4321")

        assertEquals("uuid-1", helper.getOrCreateClientId())
        assertEquals("4321", helper.getLobbyId())
    }
}
