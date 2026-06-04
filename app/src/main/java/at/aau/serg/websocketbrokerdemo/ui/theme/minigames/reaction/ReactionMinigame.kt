package at.aau.serg.websocketbrokerdemo.ui.theme.minigames.reaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class ReactionScreenState {
    READY,
    COUNTDOWN,
    REACTION,
    RESULT
}

data class ReactionPlayerUiState(
    val playerName: String,
    val avatar: ImageBitmap?,
    val isReady: Boolean = false,
    val hasPressed: Boolean = false,
    val reactionTimeMs: Int? = null
)

@Composable
fun ReactionMinigame(
    playerNames: List<String>,
    playerAvatars: List<ImageBitmap?>,
    currentPlayerName: String,
    canFinishMinigame: Boolean,
    onFinishMinigame: (winnerPlayerId: String) -> Unit
) {
    var currentScreen by remember { mutableStateOf(ReactionScreenState.READY) }
    var countdownValue by remember { mutableStateOf(3) }
    var localReactionStartTime by remember { mutableStateOf(0L) }
    var reactionButtonVisible by remember { mutableStateOf(false) }

    val players = remember(playerNames, playerAvatars) {
        mutableStateMapOf<String, ReactionPlayerUiState>().apply {
            playerNames.forEachIndexed { index, playerName ->
                put(
                    playerName,
                    ReactionPlayerUiState(
                        playerName = playerName,
                        avatar = playerAvatars.getOrNull(index)
                    )
                )
            }
        }
    }

    val playerList = players.values.toList()

    LaunchedEffect(currentScreen) {
        if (currentScreen == ReactionScreenState.COUNTDOWN) {
            countdownValue = 3
            delay(800)
            countdownValue = 2
            delay(800)
            countdownValue = 1
            delay(800)
            currentScreen = ReactionScreenState.REACTION
        }

        if (currentScreen == ReactionScreenState.REACTION) {
            reactionButtonVisible = false
            delay(Random.nextLong(900, 1800))
            reactionButtonVisible = true
            localReactionStartTime = System.currentTimeMillis()
        }
    }

    when (currentScreen) {
        ReactionScreenState.READY -> {
            ReactionLobbyScreen(
                players = playerList,
                currentPlayerName = currentPlayerName,
                onReadyClick = {
                    players[currentPlayerName]?.let { currentPlayer ->
                        players[currentPlayerName] = currentPlayer.copy(isReady = true)
                    }

                    // App-only Prototyp:
                    // Später wartet hier der Server wirklich auf alle Spieler.
                    playerNames.forEach { playerName ->
                        players[playerName]?.let { player ->
                            players[playerName] = player.copy(isReady = true)
                        }
                    }

                    currentScreen = ReactionScreenState.COUNTDOWN
                }
            )
        }

        ReactionScreenState.COUNTDOWN -> {
            ReactionCountdownScreen(
                countdownValue = countdownValue,
                players = playerList
            )
        }

        ReactionScreenState.REACTION -> {
            ReactionGameScreen(
                players = playerList,
                buttonVisible = reactionButtonVisible,
                onReactionClick = {
                    val ownReactionTime = (System.currentTimeMillis() - localReactionStartTime)
                        .toInt()
                        .coerceAtLeast(120)

                    players[currentPlayerName]?.let { currentPlayer ->
                        players[currentPlayerName] = currentPlayer.copy(
                            hasPressed = true,
                            reactionTimeMs = ownReactionTime
                        )
                    }

                    // App-only Prototyp:
                    // Andere Spieler bekommen simulierte Zeiten.
                    playerNames
                        .filter { it != currentPlayerName }
                        .forEach { playerName ->
                            players[playerName]?.let { player ->
                                players[playerName] = player.copy(
                                    hasPressed = true,
                                    reactionTimeMs = Random.nextInt(220, 650)
                                )
                            }
                        }

                    currentScreen = ReactionScreenState.RESULT
                }
            )
        }

        ReactionScreenState.RESULT -> {
            val sortedResults = playerList.sortedBy { it.reactionTimeMs ?: Int.MAX_VALUE }
            val winnerPlayerId = sortedResults.firstOrNull()?.playerName ?: currentPlayerName

            ReactionResultScreen(
                results = sortedResults,
                canFinishMinigame = canFinishMinigame,
                onContinueClick = {
                    onFinishMinigame(winnerPlayerId)
                }
            )
        }
    }
}