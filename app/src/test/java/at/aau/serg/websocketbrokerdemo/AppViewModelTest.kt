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

    // ========== PLAYER LIST TESTS ==========

    @Test
    fun `playersList is empty initially`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        assertTrue(viewModel.playersList.value.isEmpty())
    }

    @Test
    fun `onResponse with LOBBY_FULL error sets errorMessage`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        viewModel.onResponse("""{"success":false,"message":"Lobby is full"}""")

        assertEquals("Lobby is full", viewModel.errorMessage.value)
    }

    @Test
    fun `onResponse with LOBBY_FULL error does not navigate`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        viewModel.onResponse("""{"success":false,"message":"Lobby is full"}""")

        assertEquals("login", viewModel.currentScreen.value)
    }

    // ========== LEAVE LOBBY TESTS ==========

    @Test
    fun `leaveLobby navigates to login`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.hostLobby("Host")
        viewModel.navigateTo("host")

        viewModel.leaveLobby()

        assertEquals("login", viewModel.currentScreen.value)
    }

    @Test
    fun `leaveLobby clears lobbyId`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.hostLobby("Host")

        viewModel.leaveLobby()

        assertEquals("", viewModel.lobbyId.value)
    }

    @Test
    fun `leaveLobby clears playersList`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        val response = """{"success":true,"message":"OK","lobbyId":"1234","commandType":"JOIN_LOBBY","state":{"lobbyId":"1234","players":[{"playerId":"Host"},{"playerId":"Player2"}],"phase":"LOBBY"}}"""
        viewModel.onResponse(response)

        viewModel.leaveLobby()

        assertTrue(viewModel.playersList.value.isEmpty())
    }

    @Test
    fun `leaveLobby sets isHost to false`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.hostLobby("Host")

        viewModel.leaveLobby()

        assertFalse(viewModel.isHost.value)
    }

    @Test
    fun `leaveLobby calls stomp leaveLobby when lobbyId and playerName are set`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.hostLobby("Host")
        val lobbyId = viewModel.lobbyId.value

        viewModel.leaveLobby()

        verify { mockStomp.leaveLobby(lobbyId, "Host") }
    }

    @Test
    fun `leaveLobby does not call stomp when lobbyId is blank`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        viewModel.leaveLobby()

        verify(exactly = 0) { mockStomp.leaveLobby(any(), any()) }
    }

    @Test
    fun `onResponse with LEAVE_LOBBY updates playersList`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.navigateTo("host")

        val response = """{"success":true,"message":"OK","lobbyId":"1234","commandType":"LEAVE_LOBBY","state":{"lobbyId":"1234","players":[{"playerId":"Host"}],"phase":"LOBBY"}}"""
        viewModel.onResponse(response)

        assertEquals(listOf("Host"), viewModel.playersList.value)
    }

    @Test
    fun `onResponse with LEAVE_LOBBY does not navigate away from host screen`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.navigateTo("host")

        val response = """{"success":true,"message":"OK","lobbyId":"1234","commandType":"LEAVE_LOBBY","state":{"lobbyId":"1234","players":[{"playerId":"Host"}],"phase":"LOBBY"}}"""
        viewModel.onResponse(response)

        assertEquals("host", viewModel.currentScreen.value)
    }

    @Test
    fun `onResponse with LEAVE_LOBBY does not navigate away from waiting screen`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.navigateTo("waiting")

        val response = """{"success":true,"message":"OK","lobbyId":"1234","commandType":"LEAVE_LOBBY","state":{"lobbyId":"1234","players":[{"playerId":"Player1"}],"phase":"LOBBY"}}"""
        viewModel.onResponse(response)

        assertEquals("waiting", viewModel.currentScreen.value)
    }

    // ========== LOBBY CLOSED TESTS ==========

    @Test
    fun `onResponse with LOBBY_CLOSED navigates to login`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.navigateTo("waiting")

        val response = """{"success":true,"message":"OK","lobbyId":"1234","commandType":"LOBBY_CLOSED","state":{"lobbyId":"1234","players":[],"phase":"LOBBY"}}"""
        viewModel.onResponse(response)

        assertEquals("login", viewModel.currentScreen.value)
    }

    @Test
    fun `onResponse with LOBBY_CLOSED clears playersList`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.onResponse("""{"success":true,"message":"OK","lobbyId":"1234","commandType":"JOIN_LOBBY","state":{"lobbyId":"1234","players":[{"playerId":"Host"},{"playerId":"Joiner"}],"phase":"LOBBY"}}""")

        viewModel.onResponse("""{"success":true,"message":"OK","lobbyId":"1234","commandType":"LOBBY_CLOSED","state":{"lobbyId":"1234","players":[],"phase":"LOBBY"}}""")

        assertTrue(viewModel.playersList.value.isEmpty())
    }

    @Test
    fun `onResponse with LOBBY_CLOSED clears lobbyId`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.joinLobby("1234")

        viewModel.onResponse("""{"success":true,"message":"OK","lobbyId":"1234","commandType":"LOBBY_CLOSED","state":{"lobbyId":"1234","players":[],"phase":"LOBBY"}}""")

        assertEquals("", viewModel.lobbyId.value)
    }

    @Test
    fun `onResponse with LOBBY_CLOSED navigates host to login`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.navigateTo("host")

        val response = """{"success":true,"message":"OK","lobbyId":"1234","commandType":"LOBBY_CLOSED","state":{"lobbyId":"1234","players":[],"phase":"LOBBY"}}"""
        viewModel.onResponse(response)

        assertEquals("login", viewModel.currentScreen.value)
    }

    // ========== DICE / TURN TESTS ==========

    @Test
    fun `diceValue is null initially`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        assertNull(viewModel.diceValue.value)
    }

    @Test
    fun `currentTurnPlayerId is null initially`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        assertNull(viewModel.currentTurnPlayerId.value)
    }

    @Test
    fun `onRollDice calls stomp rollDice with lobbyId and playerName`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.hostLobby("Alice")
        val lobbyId = viewModel.lobbyId.value

        viewModel.onRollDice()

        verify { mockStomp.rollDice(lobbyId, "Alice") }
    }

    @Test
    fun `onEndTurn calls stomp endTurn with correct dice value`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.hostLobby("Alice")
        val lobbyId = viewModel.lobbyId.value
        val response = """{"success":true,"message":"OK","lobbyId":"$lobbyId","commandType":"ROLL_DICE","state":{"lobbyId":"$lobbyId","players":[{"playerId":"Alice"}],"phase":"IN_TURN","currentPlayerId":"Alice","lastDiceValue":4}}"""
        viewModel.onResponse(response)

        viewModel.onEndTurn()

        verify { mockStomp.endTurn(lobbyId, "Alice", 4) }
    }

    @Test
    fun `onEndTurn does nothing when diceValue is null`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        viewModel.onEndTurn()

        verify(exactly = 0) { mockStomp.endTurn(any(), any(), any()) }
    }

    @Test
    fun `onResponse parses lastDiceValue from state`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        val response = """{"success":true,"message":"OK","lobbyId":"1234","commandType":"ROLL_DICE","state":{"lobbyId":"1234","players":[{"playerId":"Alice"}],"phase":"IN_TURN","currentPlayerId":"Alice","lastDiceValue":5}}"""
        viewModel.onResponse(response)

        assertEquals(5, viewModel.diceValue.value)
    }

    @Test
    fun `onResponse sets diceValue to null when lastDiceValue is null in JSON`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.onResponse("""{"success":true,"message":"OK","lobbyId":"1234","commandType":"ROLL_DICE","state":{"lobbyId":"1234","players":[{"playerId":"Alice"}],"phase":"IN_TURN","currentPlayerId":"Alice","lastDiceValue":3}}""")

        viewModel.onResponse("""{"success":true,"message":"OK","lobbyId":"1234","commandType":"MOVE_TOKEN","state":{"lobbyId":"1234","players":[{"playerId":"Alice"}],"phase":"IN_TURN","currentPlayerId":"Bob","lastDiceValue":null}}""")

        assertNull(viewModel.diceValue.value)
    }

    @Test
    fun `onResponse parses currentPlayerId from state`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        val response = """{"success":true,"message":"OK","lobbyId":"1234","commandType":"START_GAME","state":{"lobbyId":"1234","players":[{"playerId":"Alice"},{"playerId":"Bob"}],"phase":"IN_TURN","currentPlayerId":"Alice"}}"""
        viewModel.onResponse(response)

        assertEquals("Alice", viewModel.currentTurnPlayerId.value)
    }

    @Test
    fun `onResponse updates currentTurnPlayerId after move token`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)
        viewModel.onResponse("""{"success":true,"message":"OK","lobbyId":"1234","commandType":"START_GAME","state":{"lobbyId":"1234","players":[{"playerId":"Alice"},{"playerId":"Bob"}],"phase":"IN_TURN","currentPlayerId":"Alice","lastDiceValue":null}}""")

        viewModel.onResponse("""{"success":true,"message":"OK","lobbyId":"1234","commandType":"MOVE_TOKEN","state":{"lobbyId":"1234","players":[{"playerId":"Alice"},{"playerId":"Bob"}],"phase":"IN_TURN","currentPlayerId":"Bob","lastDiceValue":null}}""")

        assertEquals("Bob", viewModel.currentTurnPlayerId.value)
    }

    @Test
    fun `onResponse sets currentTurnPlayerId to null when field is empty`() {
        val mockStomp = mockk<MyStomp>(relaxed = true)
        val viewModel = createViewModelWithMockStomp(mockStomp)

        val response = """{"success":true,"message":"OK","lobbyId":"1234","commandType":"CREATE_LOBBY","state":{"lobbyId":"1234","players":[{"playerId":"Host"}],"phase":"LOBBY"}}"""
        viewModel.onResponse(response)

        assertNull(viewModel.currentTurnPlayerId.value)
    }

    // ========== HELPER ==========

    private fun createViewModelWithMockStomp(mockStomp: MyStomp): AppViewModel {
        return AppViewModel(mockStomp)
    }
}
