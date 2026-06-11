package at.aau.serg.websocketbrokerdemo.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults


import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

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
    onSubmitGuess: (Int) -> Unit,

    flagRoundIndex: Int,
    flagCode: String?,
    flagOptions: List<String>,
    flagCorrectName: String?,
    flagScores: Map<String, Int>,
    flagTotalTimeMs: Map<String, Long>
) {
    val otherPlayerName = opponentPlayerNames.firstOrNull() ?: targetPlayerName
    var showVsScreen by remember { mutableStateOf(true) }
    var resultType by remember { mutableStateOf<MinigameResultType?>(null) }

    val allPlayers = listOf(targetPlayerName) + opponentPlayerNames
    val isFlagGame = selectedMinigame == "FLAG_GAME"

    var displayedSubPhase by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(minigameSubPhase) {
        displayedSubPhase = minigameSubPhase
    }

    val effectiveSubPhase =
        if (displayedSubPhase == "RESULT" && !isFlagGame && guessQuestionAnswer == null) null
        else displayedSubPhase

    val overlayMinWidth = if (isFlagGame) 280.dp else 320.dp
    val overlayHorizontalPadding = if (isFlagGame) 20.dp else 32.dp
    val overlayVerticalPadding = if (isFlagGame) 14.dp else 20.dp

    LaunchedEffect(announcedWinnerPlayerId, selectedMinigame) {
        if (announcedWinnerPlayerId == null) return@LaunchedEffect
        if (selectedMinigame == "REACTION_GAME") return@LaunchedEffect

        showVsScreen = false
        resultType =
            if (announcedWinnerPlayerId == targetPlayerName) {
                MinigameResultType.TARGET_PLAYER_WINS
            } else {
                MinigameResultType.OTHER_PLAYER_WINS
            }
    }

    LaunchedEffect(Unit) {
        delay(4000)
        showVsScreen = false
    }

    Box(
        modifier = Modifier
            .widthIn(min = overlayMinWidth)
            .background(Color(0xDD000000), RoundedCornerShape(20.dp))
            .padding(horizontal = overlayHorizontalPadding, vertical = overlayVerticalPadding),
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
                                MinigamePlayerAvatar(targetPlayerName, targetPlayerAvatar)

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
                    if (isFlagGame) {
                        MinigameFlagRound(
                            subPhase = "PLAYING",
                            roundIndex = flagRoundIndex,
                            totalRounds = 5,
                            flagCode = flagCode,
                            options = flagOptions,
                            correctName = null,
                            timerEndMillis = guessTimerEndMillis,
                            timerDurationSeconds = guessTimerDurationSeconds,
                            mySubmittedIndex = guessSubmissions[myPlayerId],
                            myScore = flagScores[myPlayerId] ?: 0,
                            onSelectOption = onSubmitGuess
                        )
                    } else {
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
                }

                effectiveSubPhase == "ROUND_REVEAL" -> {
                    MinigameFlagRound(
                        subPhase = "ROUND_REVEAL",
                        roundIndex = flagRoundIndex,
                        totalRounds = 5,
                        flagCode = flagCode,
                        options = flagOptions,
                        correctName = flagCorrectName,
                        timerEndMillis = guessTimerEndMillis,
                        timerDurationSeconds = guessTimerDurationSeconds,
                        mySubmittedIndex = guessSubmissions[myPlayerId],
                        myScore = flagScores[myPlayerId] ?: 0,
                        onSelectOption = onSubmitGuess
                    )
                } else {
                    MinigamePlayingScreen(
                        questionText = guessQuestionText,
                        timerEndMillis = guessTimerEndMillis,
                        timerDurationSeconds = guessTimerDurationSeconds,
                        guessSubmissions = guessSubmissions,
                        guessSubmissionTimes = guessSubmissionTimes,
                        allPlayers = allPlayers,
                        playerAvatars = listOf(targetPlayerAvatar) + opponentPlayerAvatars,
                        myGuessSubmitted = myGuessSubmitted,
                        myPlayerId = myPlayerId,
                        onSubmitGuess = onSubmitGuess
                    )
                }

                effectiveSubPhase == "RESULT" -> {
                    if (isFlagGame) {
                        MinigameFlagResultScreen(
                            winnerPlayerId = announcedWinnerPlayerId,
                            flagScores = flagScores,
                            flagTotalTimeMs = flagTotalTimeMs,
                            allPlayers = allPlayers,
                            canFinishMinigame = canFinishMinigame,
                            onFinishMinigame = onFinishMinigame
                        )
                    } else {
                        MinigameResultScreen(
                            guessSubmissions = guessSubmissions,
                            guessSubmissionTimes = guessSubmissionTimes,
                            guessQuestionAnswer = guessQuestionAnswer,
                            winnerPlayerId = announcedWinnerPlayerId,
                            canFinishMinigame = canFinishMinigame,
                            onFinishMinigame = onFinishMinigame
                        )
                    }
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
    "FLAG_GAME" to "Guess the Flag",
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

// --- Variant B "Timer Ring" design helpers --------------------------------

private val GuessAmber = Color(0xFFF5C451)
private val GuessAmberUrgent = Color(0xFFF58F6F)
private val GuessGreen = Color(0xFF73D08A)
private val GuessWhite65 = Color(0xA8FFFFFF)
private val GuessWhite40 = Color(0x66FFFFFF)
private val GuessPlayerColors = listOf(
    Color(0xFF73D08A), Color(0xFF6FB7E8), Color(0xFFF5A65B), Color(0xFFC79BEA)
)

@Composable
private fun GuessTimerRing(
    frac: Float,
    urgent: Boolean,
    ringSize: Dp = 200.dp,
    content: @Composable () -> Unit
) {
    val ringColor by animateColorAsState(
        targetValue = if (urgent) GuessAmberUrgent else GuessAmber,
        animationSpec = tween(300),
        label = "ringColor"
    )
    // Source already updates every 50 ms — no extra animation needed
    val safeFrac = frac.coerceIn(0f, 1f)

    Box(modifier = Modifier.size(ringSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 9.dp.toPx()
            val inset = strokePx / 2f
            val arcTL = Offset(inset, inset)
            val arcSz = Size(size.width - strokePx, size.height - strokePx)

            // Track ring
            drawArc(
                color = Color(0x1AFFFFFF),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTL,
                size = arcSz,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            // Glow pass (half the original width)
            if (safeFrac > 0.01f) {
                drawArc(
                    color = ringColor.copy(alpha = 0.20f),
                    startAngle = -90f,
                    sweepAngle = 360f * safeFrac,
                    useCenter = false,
                    topLeft = arcTL,
                    size = arcSz,
                    style = Stroke(width = strokePx * 1.75f, cap = StrokeCap.Round)
                )
            }
            // Progress ring
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * safeFrac,
                useCenter = false,
                topLeft = arcTL,
                size = arcSz,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
        content()
    }
}

@Composable
private fun GuessNumberLine(
    submissions: Map<String, Int>,
    answer: Int,
    winnerPlayerId: String?
) {
    val entries = remember(submissions) { submissions.entries.toList() }
    val lo = remember(submissions, answer) {
        minOf(answer.toFloat(), entries.minOfOrNull { it.value.toFloat() } ?: answer.toFloat())
    }
    val hi = remember(submissions, answer) {
        maxOf(answer.toFloat(), entries.maxOfOrNull { it.value.toFloat() } ?: answer.toFloat())
    }
    val pad = maxOf(20f, (hi - lo) * 0.18f)
    val minVal = lo - pad
    val maxVal = hi + pad
    val range = (maxVal - minVal).let { if (it == 0f) 1f else it }
    val posOf: (Int) -> Float = { v -> ((v - minVal) / range).coerceIn(0f, 1f) }

    // Canvas: gray track + amber answer marker + colored player dots
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 6.dp)
    ) {
        val trackY = size.height * 0.72f
        val dotR = 7.dp.toPx()
        val ansX = posOf(answer) * size.width

        // Track
        drawLine(
            color = Color(0x26FFFFFF),
            start = Offset(0f, trackY),
            end = Offset(size.width, trackY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Answer marker: glow + solid line
        drawLine(
            color = GuessAmber.copy(alpha = 0.28f),
            start = Offset(ansX, 4.dp.toPx()),
            end = Offset(ansX, trackY + 4.dp.toPx()),
            strokeWidth = 6.dp.toPx()
        )
        drawLine(
            color = GuessAmber,
            start = Offset(ansX, 4.dp.toPx()),
            end = Offset(ansX, trackY + 4.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )

        // Player guess dots
        entries.forEachIndexed { index, (playerId, guess) ->
            val x = posOf(guess) * size.width
            val dotColor = GuessPlayerColors[index % GuessPlayerColors.size]
            val isWinner = playerId == winnerPlayerId
            if (isWinner) {
                drawCircle(
                    color = GuessAmber,
                    radius = dotR + 3.dp.toPx(),
                    center = Offset(x, trackY),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            drawCircle(color = dotColor, radius = dotR, center = Offset(x, trackY))
        }
    }
}

// --- Playing screen (Variant B Timer Ring) --------------------------------

@Composable
private fun MinigamePlayingScreen(
    questionText: String?,
    timerEndMillis: Long?,
    timerDurationSeconds: Int?,
    guessSubmissions: Map<String, Int>,
    guessSubmissionTimes: Map<String, Long>,
    allPlayers: List<String>,
    playerAvatars: List<ImageBitmap?>,
    myGuessSubmitted: Boolean,
    myPlayerId: String,
    onSubmitGuess: (Int) -> Unit
) {
    // ── Integer seconds for the text label (original logic, untouched) ──
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

    // ── Smooth float for the ring + capture game start for elapsed-time display ──
    var ringFrac by remember { mutableStateOf(1f) }
    var gameStartMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(timerDurationSeconds, timerEndMillis) {
        val startMs = System.currentTimeMillis()
        gameStartMs = startMs
        val totalMs = when {
            timerDurationSeconds != null -> timerDurationSeconds * 1000L
            timerEndMillis != null       -> (timerEndMillis - startMs).coerceAtLeast(1000L)
            else                         -> 30_000L
        }
        while (true) {
            delay(50L)
            val remaining = when {
                timerDurationSeconds != null ->
                    (totalMs - (System.currentTimeMillis() - startMs)).coerceAtLeast(0L)
                timerEndMillis != null ->
                    (timerEndMillis - System.currentTimeMillis()).coerceAtLeast(0L)
                else -> 0L
            }
            ringFrac = (remaining.toFloat() / totalMs).coerceIn(0f, 1f)
            if (remaining <= 0L) break
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

    val urgent = remainingSeconds in 1..6

    // ── Adaptive sizing based on window height ──
    val screenHeight = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp()
    }
    val ringSize = when {
        screenHeight < 580.dp -> 140.dp
        screenHeight < 660.dp -> 158.dp
        screenHeight < 740.dp -> 174.dp
        else                  -> 196.dp
    }
    val vGap = when {
        screenHeight < 580.dp -> 8.dp
        screenHeight < 660.dp -> 11.dp
        screenHeight < 740.dp -> 14.dp
        else                  -> 18.dp
    }
    val inputFontSize = when {
        screenHeight < 660.dp -> 38.sp
        else                  -> 46.sp
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(max = 320.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Question text
        Text(
            text = questionText ?: "",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(vGap))

        // Timer Ring hero — smooth ringFrac, input or locked value inside
        GuessTimerRing(frac = ringFrac, urgent = urgent, ringSize = ringSize) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (myGuessSubmitted) {
                    Text(
                        text = if (guessInput.isNotEmpty()) guessInput else "—",
                        color = Color.White,
                        fontSize = inputFontSize,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                } else {
                    BasicTextField(
                        value = guessInput,
                        onValueChange = { if (!inputDisabled) guessInput = it.filter { c -> c.isDigit() }.take(6) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !inputDisabled,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = inputFontSize,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.width(118.dp),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.Center) {
                                if (guessInput.isEmpty()) {
                                    Text(
                                        text = "?",
                                        color = Color(0x44FFFFFF),
                                        fontSize = inputFontSize,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${remainingSeconds}s",
                    color = if (urgent) GuessAmberUrgent else GuessWhite65,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(vGap))

        // Player submission avatars — amber ring when locked, elapsed time below
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            allPlayers.forEachIndexed { index, playerId ->
                val hasSubmitted = playerId in guessSubmissions
                val avatar = playerAvatars.getOrNull(index)
                val fallbackColor = GuessPlayerColors[index % GuessPlayerColors.size]
                val elapsedStr: String? = guessSubmissionTimes[playerId]?.let { submittedAt ->
                    val ms = (submittedAt - gameStartMs).coerceAtLeast(0L)
                    val hs = ms / 10L
                    "%02d:%02d".format(hs / 100L, hs % 100L)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(fallbackColor.copy(alpha = if (hasSubmitted) 1f else 0.30f))
                            .then(
                                if (hasSubmitted) Modifier.border(2.dp, GuessAmber, CircleShape)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatar != null) {
                            Image(
                                bitmap = avatar,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = playerId.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Text(
                        text = elapsedStr ?: "—",
                        color = if (elapsedStr != null) GuessWhite65 else Color(0x2EFFFFFF),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (index != allPlayers.lastIndex) Spacer(Modifier.width(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(vGap))

        if (myGuessSubmitted) {
            Text(
                text = "✓  Gesperrt – warte auf andere…",
                color = GuessGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Button(
                onClick = {
                    val guess = guessInput.toIntOrNull() ?: return@Button
                    onSubmitGuess(guess)
                },
                enabled = !inputDisabled && guessInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GuessAmber,
                    disabledContainerColor = Color(0xFF3A3520)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .width(176.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = "Absenden",
                    color = Color(0xFF0C1622),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        // Bottom padding so scroll always reveals the button above the nav bar
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// --- Result screen (Variant B: count-up answer + number line) -------------

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

    // Count-up animation for the revealed answer
    var displayedAnswer by remember(guessQuestionAnswer) { mutableStateOf(0) }
    LaunchedEffect(guessQuestionAnswer) {
        val target = guessQuestionAnswer ?: return@LaunchedEffect
        val steps = 28
        for (i in 1..steps) {
            displayedAnswer = target * i / steps
            delay(28L)
        }
        displayedAnswer = target
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(max = 320.dp)
    ) {
        // "THE ANSWER" label
        Text(
            text = "DIE ANTWORT",
            color = GuessWhite40,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Animated count-up number
        Text(
            text = "$displayedAnswer",
            color = GuessAmber,
            fontSize = 44.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Number line: plots each guess relative to the correct answer
        if (guessSubmissions.isNotEmpty()) {
            GuessNumberLine(
                submissions = guessSubmissions,
                answer = correctAnswer,
                winnerPlayerId = winnerPlayerId
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Ranked player rows
        if (sortedEntries.isEmpty()) {
            Text(
                text = "Keine Einsendungen",
                color = GuessWhite65,
                fontSize = 15.sp
            )
        } else {
            sortedEntries.forEachIndexed { rank, (playerId, guess) ->
                val distance = abs(guess - correctAnswer)
                val isWinner = playerId == winnerPlayerId
                val timeStr = guessSubmissionTimes[playerId]
                    ?.let { timeFmt.format(java.util.Date(it)) }
                    ?: "—"

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isWinner) Color(0xFF43A047).copy(alpha = 0.22f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isWinner) "🏆" else "${rank + 1}.",
                        fontSize = 14.sp,
                        color = if (isWinner) GuessAmber else GuessWhite65,
                        modifier = Modifier.width(26.dp)
                    )
                    Text(
                        text = playerId,
                        color = if (isWinner) GuessAmber else Color.White,
                        fontSize = 15.sp,
                        fontWeight = if (isWinner) FontWeight.ExtraBold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$guess  (±$distance)",
                            color = if (isWinner) GuessAmber else GuessWhite65,
                            fontSize = 13.sp,
                            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = "⏱ $timeStr",
                            color = GuessWhite40,
                            fontSize = 10.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (canFinishMinigame) {
            Button(
                onClick = { onFinishMinigame(winnerPlayerId ?: "") },
                colors = ButtonDefaults.buttonColors(containerColor = GuessAmber),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .width(200.dp)
                    .height(52.dp)
            ) {
                Text(
                    text = "Weiter",
                    color = Color(0xFF0C1622),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

private fun formatFlagTime(ms: Long?): String =
    if (ms == null || ms <= 0L) "—" else "%.1fs".format(ms / 1000.0)

@Composable
private fun MinigameFlagResultScreen(
    winnerPlayerId: String?,
    flagScores: Map<String, Int>,
    flagTotalTimeMs: Map<String, Long>,
    allPlayers: List<String>,
    canFinishMinigame: Boolean,
    onFinishMinigame: (String) -> Unit
) {
    val ranked = allPlayers.sortedWith(
        compareByDescending<String> { flagScores[it] ?: 0 }
            .thenBy { flagTotalTimeMs[it] ?: Long.MAX_VALUE }
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = winnerPlayerId?.let { "🏆 $it wins!" } ?: "Result",
            color = Color(0xFFD4AF37),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        ranked.forEach { playerId ->
            val isWinner = playerId == winnerPlayerId
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        if (isWinner) Color(0xFF43A047).copy(alpha = 0.3f) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = if (isWinner) "🏆 " else "   ", fontSize = 16.sp)
                Text(
                    text = playerId,
                    color = if (isWinner) Color(0xFFD4AF37) else Color.White,
                    fontSize = 16.sp,
                    fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.width(140.dp)
                )
                Text(
                    text = "${flagScores[playerId] ?: 0} / 5",
                    color = if (isWinner) Color(0xFFD4AF37) else Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "⏱ ${formatFlagTime(flagTotalTimeMs[playerId])}",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
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