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
    reactionReadyPlayerIds: List<String>,
    reactionReadyEndsAtMs: Long?,
    reactionStartTimeMs: Long?,
    reactionPressTimesMs: Map<String, Long>,
    reactionButtonVisibleAtMs: Long?,
    reactionRoundEndsAtMs: Long?,
    minigameWinnerPlayerId: String?,
    onReactionReady: () -> Unit,
    onReactionPress: () -> Unit,
    onFinishMinigame: (winnerPlayerId: String) -> Unit
) {
    var currentScreen by remember { mutableStateOf(ReactionScreenState.READY) }
    var countdownValue by remember { mutableStateOf(3) }
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

    LaunchedEffect(reactionReadyPlayerIds) {
        playerNames.forEach { playerName ->
            players[playerName]?.let { player ->
                players[playerName] = player.copy(
                    isReady = reactionReadyPlayerIds.contains(playerName)
                )
            }
        }
    }

    LaunchedEffect(reactionReadyEndsAtMs) {
        if (
            reactionReadyEndsAtMs != null &&
            currentScreen == ReactionScreenState.READY
        ) {
            val delayMs =
                reactionReadyEndsAtMs - System.currentTimeMillis()

            if (delayMs > 0) {
                delay(delayMs)
            }

            onReactionReady()
        }
    }

    LaunchedEffect(reactionStartTimeMs) {
        if (reactionStartTimeMs != null && currentScreen == ReactionScreenState.READY) {
            currentScreen = ReactionScreenState.COUNTDOWN
        }
    }

    LaunchedEffect(reactionPressTimesMs) {
        playerNames.forEach { playerName ->
            players[playerName]?.let { player ->
                val serverReactionTime = reactionPressTimesMs[playerName]

                players[playerName] = player.copy(
                    hasPressed = serverReactionTime != null,
                    reactionTimeMs = serverReactionTime?.toInt()
                )
            }
        }

        val allPlayersPressed =
            playerNames.isNotEmpty() &&
                    playerNames.all { playerName ->
                        reactionPressTimesMs.containsKey(playerName)
                    }

        if (allPlayersPressed && currentScreen == ReactionScreenState.REACTION) {
            currentScreen = ReactionScreenState.RESULT
        }
    }

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
    }

    LaunchedEffect(reactionButtonVisibleAtMs, currentScreen) {
        if (
            currentScreen == ReactionScreenState.REACTION &&
            reactionButtonVisibleAtMs != null
        ) {
            val delayMs =
                reactionButtonVisibleAtMs - System.currentTimeMillis()

            if (delayMs > 0) {
                delay(delayMs)
            }

            reactionButtonVisible = true
        }
    }

    LaunchedEffect(reactionRoundEndsAtMs, currentScreen) {
        if (
            currentScreen == ReactionScreenState.REACTION &&
            reactionRoundEndsAtMs != null
        ) {
            val delayMs = reactionRoundEndsAtMs - System.currentTimeMillis()

            if (delayMs > 0) {
                delay(delayMs)
            }

            onReactionPress()
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

                    onReactionReady()
                    // Der Countdown startet später automatisch, sobald der Server reactionStartTimeMs setzt.
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
                    onReactionPress()

                    players[currentPlayerName]?.let { currentPlayer ->
                        players[currentPlayerName] = currentPlayer.copy(
                            hasPressed = true
                        )
                    }
                }
            )
        }

        ReactionScreenState.RESULT -> {
            val sortedResults = playerList.sortedBy { it.reactionTimeMs ?: Int.MAX_VALUE }
            val winnerPlayerId = minigameWinnerPlayerId
                ?: sortedResults.firstOrNull()?.playerName
                ?: currentPlayerName

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