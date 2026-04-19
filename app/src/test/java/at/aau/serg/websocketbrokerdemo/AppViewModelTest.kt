package at.aau.serg.websocketbrokerdemo

import MyStomp
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== NAVIGATION TESTS ==========

    @Test
    fun `navigateTo changes currentScreen`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        viewModel.navigateTo("lobby")

        assertEquals("lobby", viewModel.currentScreen.value)
    }

    @Test
    fun `initial screen is login`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        assertEquals("login", viewModel.currentScreen.value)
    }

    // ========== SET PLAYER NAME TESTS ==========

    @Test
    fun `setPlayerName updates playerName state`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        viewModel.setPlayerName("TestPlayer")

        assertEquals("TestPlayer", viewModel.playerName.value)
    }

    // ========== HOST LOBBY TESTS ==========

    @Test
    fun `hostLobby sets isHost to true`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        viewModel.hostLobby("HostPlayer")

        assertTrue(viewModel.isHost.value)
    }

    @Test
    fun `hostLobby sets isLoading to true`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        viewModel.hostLobby("HostPlayer")

        assertTrue(viewModel.isLoading.value)
    }

    @Test
    fun `hostLobby sets playerName`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        viewModel.hostLobby("HostPlayer")

        assertEquals("HostPlayer", viewModel.playerName.value)
    }

    @Test
    fun `hostLobby generates 4-digit lobbyId`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        viewModel.hostLobby("HostPlayer")

        val lobbyId = viewModel.lobbyId.value
        assertTrue(lobbyId.length == 4)
        assertTrue(lobbyId.all { it.isDigit() })
    }

    @Test
    fun `hostLobby calls createMultiplayerLobby on stomp`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        viewModel.hostLobby("HostPlayer")

        verify { mockStomp.createMultiplayerLobby(any(), "HostPlayer") }
    }

    @Test
    fun `hostLobby clears previous error`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        // Simulate previous error
        viewModel.onResponse("""{"success":false,"message":"Previous error"}""")

        viewModel.hostLobby("HostPlayer")

        assertNull(viewModel.errorMessage.value)
    }

    // ========== JOIN LOBBY TESTS ==========

    @Test
    fun `joinLobby sets isHost to false`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        viewModel.joinLobby("1234")

        assertFalse(viewModel.isHost.value)
    }

    @Test
    fun `joinLobby sets isLoading to true`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        viewModel.joinLobby("1234")

        assertTrue(viewModel.isLoading.value)
    }

    @Test
    fun `joinLobby sets lobbyId from parameter`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        viewModel.joinLobby("5678")

        assertEquals("5678", viewModel.lobbyId.value)
    }

    @Test
    fun `joinLobby calls joinMultiplayerLobby on stomp`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.setPlayerName("JoinPlayer")

        viewModel.joinLobby("1234")

        verify { mockStomp.joinMultiplayerLobby("1234", "JoinPlayer") }
    }

    // ========== ON RESPONSE TESTS - ERROR HANDLING ==========

    @Test
    fun `onResponse with success false sets errorMessage`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        viewModel.onResponse("""{"success":false,"message":"Lobby does not exist"}""")

        assertEquals("Lobby does not exist", viewModel.errorMessage.value)
    }

    @Test
    fun `onResponse with success false sets isLoading to false`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.joinLobby("1234") // Sets isLoading to true

        viewModel.onResponse("""{"success":false,"message":"Error"}""")

        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `onResponse with success false does not navigate`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        viewModel.onResponse("""{"success":false,"message":"Error"}""")

        assertEquals("login", viewModel.currentScreen.value)
    }

    // ========== ON RESPONSE TESTS - SUCCESS HANDLING ==========

    @Test
    fun `onResponse with CREATE_LOBBY success navigates to host`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        val response = """{"success":true,"message":"OK","lobbyId":"1234","commandType":"CREATE_LOBBY","state":{"lobbyId":"1234","players":[{"playerId":"Host"}],"phase":"LOBBY"}}"""
        viewModel.onResponse(response)

        assertEquals("host", viewModel.currentScreen.value)
    }

    @Test
    fun `onResponse with JOIN_LOBBY success navigates to waiting`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        val response = """{"success":true,"message":"OK","lobbyId":"1234","commandType":"JOIN_LOBBY","state":{"lobbyId":"1234","players":[{"playerId":"Host"},{"playerId":"Joiner"}],"phase":"LOBBY"}}"""
        viewModel.onResponse(response)

        assertEquals("waiting", viewModel.currentScreen.value)
    }

    @Test
    fun `onResponse with START_GAME navigates to game`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        val response = """{"success":true,"message":"OK","lobbyId":"1234","commandType":"START_GAME","state":{"lobbyId":"1234","players":[{"playerId":"Host"},{"playerId":"Joiner"}],"phase":"IN_TURN"}}"""
        viewModel.onResponse(response)

        assertEquals("game", viewModel.currentScreen.value)
    }

    @Test
    fun `onResponse updates playersList from state`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        val response = """{"success":true,"message":"OK","lobbyId":"1234","commandType":"JOIN_LOBBY","state":{"lobbyId":"1234","players":[{"playerId":"Player1"},{"playerId":"Player2"},{"playerId":"Player3"}],"phase":"LOBBY"}}"""
        viewModel.onResponse(response)

        assertEquals(listOf("Player1", "Player2", "Player3"), viewModel.playersList.value)
    }

    @Test
    fun `onResponse sets isLoading to false on success`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.hostLobby("Host") // Sets isLoading to true

        val response = """{"success":true,"message":"OK","lobbyId":"1234","commandType":"CREATE_LOBBY","state":{"lobbyId":"1234","players":[{"playerId":"Host"}],"phase":"LOBBY"}}"""
        viewModel.onResponse(response)

        assertFalse(viewModel.isLoading.value)
    }

    // ========== ON RESPONSE TESTS - EDGE CASES ==========

    @Test
    fun `onResponse with Error prefix sets errorMessage`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        viewModel.onResponse("Error: Connection failed")

        assertEquals("Error: Connection failed", viewModel.errorMessage.value)
    }

    @Test
    fun `onResponse with invalid JSON sets error`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        viewModel.onResponse("{invalid json")

        assertEquals("Fehler bei Server-Kommunikation", viewModel.errorMessage.value)
    }

    @Test
    fun `onResponse JOIN_LOBBY does not navigate if already on host screen`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.navigateTo("host")

        val response = """{"success":true,"message":"OK","lobbyId":"1234","commandType":"JOIN_LOBBY","state":{"lobbyId":"1234","players":[{"playerId":"Host"},{"playerId":"Joiner"}],"phase":"LOBBY"}}"""
        viewModel.onResponse(response)

        // Should stay on host screen, not navigate to waiting
        assertEquals("host", viewModel.currentScreen.value)
    }

    // ========== CLEAR ERROR TESTS ==========

    @Test
    fun `clearError sets errorMessage to null`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.onResponse("""{"success":false,"message":"Some error"}""")

        viewModel.clearError()

        assertNull(viewModel.errorMessage.value)
    }

    // ========== START GAME TESTS ==========

    @Test
    fun `startGame calls startGameCmd on stomp`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.hostLobby("Host")
        val lobbyId = viewModel.lobbyId.value

        viewModel.startGame()

        verify { mockStomp.startGameCmd(lobbyId, any()) }
    }

    // ========== HELPER ==========

    private fun createViewModelWithMockStomp(mockStomp: MyStomp): AppViewModel {
        return AppViewModel(mockStomp)
    }
}
