package at.aau.serg.websocketbrokerdemo.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.websocketbrokerdemo.ui.theme.minigames.reaction.ReactionMinigame
import com.example.myapplication.R
import kotlinx.coroutines.delay
import kotlin.math.abs

private enum class MinigameResultType {
    TARGET_PLAYER_WINS,
    OTHER_PLAYER_WINS
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun MinigameOverlay(
    targetPlayerName: String,
    opponentPlayerNames: List<String>,
    targetCityName: String,
    targetPlayerAvatar: ImageBitmap?,
    opponentPlayerAvatars: List<ImageBitmap?>,
    currentPlayerName: String,
    announcedWinnerPlayerId: String?,
    canFinishMinigame: Boolean,

    reactionReadyPlayerIds: List<String>,
    reactionReadyEndsAtMs: Long?,
    reactionStartTimeMs: Long?,
    reactionPressTimesMs: Map<String, Long>,
    reactionButtonVisibleAtMs: Long?,
    reactionRoundEndsAtMs: Long?,
    serverNowMs: Long?,
    onReactionReady: () -> Unit,
    onReactionPress: () -> Unit,

    onAnnounceMinigameResult: (winnerPlayerId: String) -> Unit,
    onFinishMinigame: (winnerPlayerId: String) -> Unit,

    minigameSubPhase: String?,
    selectedMinigame: String?,
    guessQuestionText: String?,
    guessQuestionAnswer: Int?,
    guessTimerEndMillis: Long?,
    guessTimerDurationSeconds: Int?,
    guessSubmissions: Map<String, Int>,
    guessSubmissionTimes: Map<String, Long>,
    myGuessSubmitted: Boolean,
    myPlayerId: String,
    onSubmitGuess: (Int) -> Unit
) {
    val otherPlayerName = opponentPlayerNames.firstOrNull() ?: targetPlayerName
    var showVsScreen by remember { mutableStateOf(true) }
    var resultType by remember { mutableStateOf<MinigameResultType?>(null) }

    val allPlayers = listOf(targetPlayerName) + opponentPlayerNames

    var displayedSubPhase by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(minigameSubPhase) {
        displayedSubPhase = minigameSubPhase
    }

    val effectiveSubPhase =
        if (displayedSubPhase == "RESULT" && guessQuestionAnswer == null) null
        else displayedSubPhase

    LaunchedEffect(announcedWinnerPlayerId) {
        if (announcedWinnerPlayerId == null) return@LaunchedEffect

        showVsScreen = false
        resultType =
            if (announcedWinnerPlayerId == targetPlayerName) {
                MinigameResultType.TARGET_PLAYER_WINS
            } else {
                MinigameResultType.OTHER_PLAYER_WINS
            }
    }

    LaunchedEffect(Unit) {
        delay(2000)
        showVsScreen = false
    }

    Box(
        modifier = Modifier
            .widthIn(min = 320.dp)
            .background(Color(0xDD000000), RoundedCornerShape(20.dp))
            .padding(horizontal = 32.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when {
                effectiveSubPhase == "SELECTING" -> {
                    MinigameSelectingScreen(selectedMinigame)
                }

                selectedMinigame == "REACTION_GAME" || selectedMinigame == null -> {
                    when {
                        showVsScreen -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                MinigamePlayerAvatar(
                                    playerName = targetPlayerName,
                                    avatar = targetPlayerAvatar
                                )

                                Spacer(modifier = Modifier.width(36.dp))

                                Text(
                                    text = stringResource(R.string.minigame_vs_title),
                                    color = Color.White,
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.width(36.dp))

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    opponentPlayerNames.forEachIndexed { index, opponentName ->
                                        MinigamePlayerAvatar(
                                            playerName = opponentName,
                                            avatar = opponentPlayerAvatars.getOrNull(index)
                                        )

                                        if (index != opponentPlayerNames.lastIndex) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    }
                                }
                            }
                        }

                        resultType == MinigameResultType.TARGET_PLAYER_WINS -> {
                            ReactionResultInfoScreen(
                                title = stringResource(R.string.minigame_result_target_wins_title),
                                text = stringResource(
                                    R.string.minigame_result_target_wins_text,
                                    targetPlayerName,
                                    targetCityName.ifBlank { "the target city" }
                                ),
                                playerNames = listOf(targetPlayerName),
                                playerAvatars = listOf(targetPlayerAvatar),
                                onContinue = { onFinishMinigame(targetPlayerName) }
                            )
                        }

                        resultType == MinigameResultType.OTHER_PLAYER_WINS -> {
                            ReactionResultInfoScreen(
                                title = stringResource(R.string.minigame_result_other_wins_title),
                                text = stringResource(
                                    R.string.minigame_result_other_wins_text,
                                    otherPlayerName,
                                    targetPlayerName,
                                    targetCityName.ifBlank { "the target city" }
                                ),
                                playerNames = opponentPlayerNames,
                                playerAvatars = opponentPlayerAvatars,
                                onContinue = { onFinishMinigame(otherPlayerName) }
                            )
                        }

                        else -> {
                            ReactionMinigame(
                                playerNames = allPlayers,
                                playerAvatars = listOf(targetPlayerAvatar) + opponentPlayerAvatars,
                                currentPlayerName = currentPlayerName,
                                canFinishMinigame = canFinishMinigame,
                                reactionReadyPlayerIds = reactionReadyPlayerIds,
                                reactionReadyEndsAtMs = reactionReadyEndsAtMs,
                                reactionStartTimeMs = reactionStartTimeMs,
                                reactionPressTimesMs = reactionPressTimesMs,
                                reactionButtonVisibleAtMs = reactionButtonVisibleAtMs,
                                reactionRoundEndsAtMs = reactionRoundEndsAtMs,
                                serverNowMs = serverNowMs,
                                minigameWinnerPlayerId = announcedWinnerPlayerId,
                                onReactionReady = onReactionReady,
                                onReactionPress = onReactionPress,
                                onFinishMinigame = { winnerPlayerId ->
                                    resultType =
                                        if (winnerPlayerId == targetPlayerName) {
                                            MinigameResultType.TARGET_PLAYER_WINS
                                        } else {
                                            MinigameResultType.OTHER_PLAYER_WINS
                                        }
                                }
                            )
                        }
                    }
                }

                effectiveSubPhase == "PLAYING" -> {
                    MinigamePlayingScreen(
                        questionText = guessQuestionText,
                        timerEndMillis = guessTimerEndMillis,
                        timerDurationSeconds = guessTimerDurationSeconds,
                        guessSubmissions = guessSubmissions,
                        allPlayers = allPlayers,
                        myGuessSubmitted = myGuessSubmitted,
                        myPlayerId = myPlayerId,
                        onSubmitGuess = onSubmitGuess
                    )
                }

                effectiveSubPhase == "RESULT" -> {
                    MinigameResultScreen(
                        guessSubmissions = guessSubmissions,
                        guessSubmissionTimes = guessSubmissionTimes,
                        guessQuestionAnswer = guessQuestionAnswer,
                        winnerPlayerId = announcedWinnerPlayerId,
                        canFinishMinigame = canFinishMinigame,
                        onFinishMinigame = onFinishMinigame
                    )
                }

                else -> {
                    Text(
                        text = "Minispiel wird geladen...",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ReactionResultInfoScreen(
    title: String,
    text: String,
    playerNames: List<String>,
    playerAvatars: List<ImageBitmap?>,
    onContinue: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            playerNames.forEachIndexed { index, playerName ->
                MinigamePlayerAvatar(
                    playerName = playerName,
                    avatar = playerAvatars.getOrNull(index)
                )

                if (index != playerNames.lastIndex) {
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.width(48.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(26.dp))

            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8DB6CD)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .width(220.dp)
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.minigame_result_continue_button),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private val KNOWN_MINIGAMES = listOf(
    "GUESS_GAME" to "Schätzspiel",
    "QUIZ_GAME" to "Quizspiel",
    "MEMORY_GAME" to "Memory",
    "REACTION_GAME" to "Reaktionsspiel",
)

@Composable
private fun MinigameSelectingScreen(selectedMinigame: String?) {
    val targetIndex = KNOWN_MINIGAMES.indexOfFirst { it.first == selectedMinigame }
        .coerceAtLeast(0)

    var highlightedIndex by remember { mutableStateOf(0) }
    var revealed by remember { mutableStateOf(false) }

    LaunchedEffect(selectedMinigame) {
        revealed = false
        highlightedIndex = 0
        val totalSteps = 24
        for (i in 0 until totalSteps) {
            highlightedIndex = i % KNOWN_MINIGAMES.size
            delay(
                when {
                    i < totalSteps * 0.50 -> 70L
                    i < totalSteps * 0.75 -> 150L
                    else -> 260L
                }
            )
        }
        highlightedIndex = targetIndex
        revealed = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(min = 280.dp)
    ) {
        Text(
            text = if (revealed) "Minispiel ausgewählt!" else "Minispiel wird ausgewählt...",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        for (row in 0..1) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..1) {
                    val idx = row * 2 + col
                    val (_, name) = KNOWN_MINIGAMES[idx]
                    val isHighlighted = highlightedIndex == idx
                    val isSelected = revealed && idx == targetIndex

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(5.dp)
                            .background(
                                color = when {
                                    isSelected -> Color(0xFF1B5E20)
                                    isHighlighted -> Color(0xFF37474F)
                                    else -> Color(0xFF263238)
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = if (isHighlighted || isSelected) 2.dp else 0.dp,
                                color = if (isSelected) Color(0xFFD4AF37) else Color.White,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name,
                            color = when {
                                isSelected -> Color(0xFFD4AF37)
                                isHighlighted -> Color.White
                                else -> Color(0xFF78909C)
                            },
                            fontSize = 15.sp,
                            fontWeight = if (isSelected || isHighlighted) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (row == 0) Spacer(modifier = Modifier.height(2.dp))
        }

        if (revealed) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Viel Erfolg!",
                color = Color.LightGray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun MinigamePlayingScreen(
    questionText: String?,
    timerEndMillis: Long?,
    timerDurationSeconds: Int?,
    guessSubmissions: Map<String, Int>,
    allPlayers: List<String>,
    myGuessSubmitted: Boolean,
    myPlayerId: String,
    onSubmitGuess: (Int) -> Unit
) {
    var remainingSeconds by remember(timerDurationSeconds, timerEndMillis) {
        mutableStateOf(
            timerDurationSeconds
                ?: ((timerEndMillis ?: 0L) - System.currentTimeMillis()).div(1000L)
                    .coerceAtLeast(0L).toInt()
        )
    }

    LaunchedEffect(timerDurationSeconds, timerEndMillis) {
        if (timerDurationSeconds != null) {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds = (remainingSeconds - 1).coerceAtLeast(0)
            }
        } else {
            while (true) {
                delay(500L)
                val r = ((timerEndMillis ?: 0L) - System.currentTimeMillis())
                    .div(1000L).coerceAtLeast(0L).toInt()
                remainingSeconds = r
                if (r <= 0) break
            }
        }
    }

    val timerExpired = remainingSeconds <= 0
    val inputDisabled = myGuessSubmitted || timerExpired
    var guessInput by remember { mutableStateOf("") }

    LaunchedEffect(timerExpired) {
        if (!timerExpired) return@LaunchedEffect
        if (myGuessSubmitted) return@LaunchedEffect
        val typedGuess = guessInput.toIntOrNull()
        when {
            typedGuess != null -> onSubmitGuess(typedGuess)
            guessSubmissions.isEmpty() && myPlayerId == allPlayers.firstOrNull() -> onSubmitGuess(0)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = questionText ?: "",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Zeit: ${remainingSeconds}s",
            color = if (remainingSeconds in 1..10) Color(0xFFE53935) else Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = guessInput,
            onValueChange = { if (!inputDisabled) guessInput = it.filter { c -> c.isDigit() } },
            label = { Text("Deine Schätzung", color = Color.LightGray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !inputDisabled,
            singleLine = true,
            modifier = Modifier.width(200.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color.Gray,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                disabledBorderColor = Color.DarkGray
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val guess = guessInput.toIntOrNull() ?: return@Button
                onSubmitGuess(guess)
            },
            enabled = !inputDisabled && guessInput.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8DB6CD)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .width(180.dp)
                .height(52.dp)
        ) {
            Text(
                text = "Absenden",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            allPlayers.forEachIndexed { index, playerId ->
                val hasSubmitted = playerId in guessSubmissions
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (hasSubmitted) Color(0xFF43A047) else Color.Gray)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = playerId,
                    color = if (hasSubmitted) Color(0xFF43A047) else Color.LightGray,
                    fontSize = 12.sp
                )
                if (index != allPlayers.lastIndex) {
                    Spacer(modifier = Modifier.width(14.dp))
                }
            }
        }
    }
}

@Composable
private fun MinigameResultScreen(
    guessSubmissions: Map<String, Int>,
    guessSubmissionTimes: Map<String, Long>,
    guessQuestionAnswer: Int?,
    winnerPlayerId: String?,
    canFinishMinigame: Boolean,
    onFinishMinigame: (String) -> Unit
) {
    val correctAnswer = guessQuestionAnswer ?: 0
    val sortedEntries = guessSubmissions.entries.sortedBy { (_, guess) -> abs(guess - correctAnswer) }
    val timeFmt = remember { java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Richtige Antwort: $correctAnswer",
            color = Color(0xFFD4AF37),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (sortedEntries.isEmpty()) {
            Text(
                text = "Keine Einsendungen",
                color = Color.LightGray,
                fontSize = 15.sp
            )
        } else {
            sortedEntries.forEach { (playerId, guess) ->
                val distance = abs(guess - correctAnswer)
                val isWinner = playerId == winnerPlayerId
                val timeStr = guessSubmissionTimes[playerId]
                    ?.let { timeFmt.format(java.util.Date(it)) }
                    ?: "--:--:--"

                Column(
                    modifier = Modifier
                        .background(
                            if (isWinner) Color(0xFF43A047).copy(alpha = 0.3f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isWinner) "🏆 " else "   ",
                            fontSize = 16.sp
                        )
                        Text(
                            text = playerId,
                            color = if (isWinner) Color(0xFFD4AF37) else Color.White,
                            fontSize = 16.sp,
                            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.width(100.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "$guess  (Δ$distance)",
                            color = if (isWinner) Color(0xFFD4AF37) else Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "⏱ $timeStr",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (canFinishMinigame) {
            Button(
                onClick = { onFinishMinigame(winnerPlayerId ?: "") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8DB6CD)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .width(220.dp)
                    .height(56.dp)
            ) {
                Text(
                    text = "Weiter",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MinigamePlayerAvatar(
    playerName: String,
    avatar: ImageBitmap?
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            if (avatar != null) {
                Image(
                    bitmap = avatar,
                    contentDescription = playerName,
                    modifier = Modifier.size(72.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = playerName.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = playerName,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}