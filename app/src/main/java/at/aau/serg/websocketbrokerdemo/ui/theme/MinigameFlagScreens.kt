package at.aau.serg.websocketbrokerdemo.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Im selben Design wie das Overlay: dunkler Grund, Stahlblau-/Gold-Akzente.
private val FlagOptionColors = listOf(
    Color(0xFF8DB6CD), // Stahlblau (App-Primärfarbe)
    Color(0xFF5B8A72), // gedämpftes Grün
    Color(0xFFC9A227), // gedämpftes Gold
    Color(0xFFB0606A)  // gedämpftes Rosé
)
private val FlagCorrect = Color(0xFF43A047)
private val FlagWrong = Color(0xFFC62828)
private val FlagAccent = Color(0xFFD4AF37)

/**
 * Eine Flaggen-Runde (Kahoot-Stil) für die PLAYING- und ROUND_REVEAL-Sub-Phase.
 * Zeigt die Flagge, 4 Optionen, einen Countdown und – in der Auflösung – die
 * richtige Antwort (grün) sowie die eigene falsche Wahl (rot).
 */
@Composable
fun MinigameFlagRound(
    subPhase: String?,
    roundIndex: Int,
    totalRounds: Int,
    flagCode: String?,
    options: List<String>,
    correctName: String?,
    timerEndMillis: Long?,
    timerDurationSeconds: Int?,
    mySubmittedIndex: Int?,
    myScore: Int,
    onSelectOption: (Int) -> Unit
) {
    val isReveal = subPhase == "ROUND_REVEAL"
    val correctIndex = remember(correctName, options) { options.indexOf(correctName) }
    val configuration = LocalConfiguration.current
    val sizeScale = remember(configuration.screenHeightDp) {
        when {
            configuration.screenHeightDp < 600 -> 0.72f
            configuration.screenHeightDp < 680 -> 0.82f
            configuration.screenHeightDp < 760 -> 0.92f
            else -> 1f
        }
    }

    // Lokale Auswahl: sperrt die Kacheln sofort (vor dem Server-Echo). Reset je Runde.
    var localPick by remember(roundIndex) { mutableStateOf<Int?>(null) }
    val effectivePick = mySubmittedIndex ?: localPick

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(300.dp.scaled(sizeScale))
    ) {
        // Kopfzeile: Runde + eigener Punktestand
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Flag ${roundIndex + 1} / $totalRounds",
                color = FlagAccent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Score: $myScore",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(14.dp.scaled(sizeScale)))

        FlagImage(flagCode, sizeScale)

        Spacer(modifier = Modifier.height(14.dp.scaled(sizeScale)))

        if (isReveal) {
            RevealFeedback(answered = effectivePick != null, correct = effectivePick == correctIndex)
        } else {
            FlagCountdown(timerEndMillis, timerDurationSeconds)
        }

        Spacer(modifier = Modifier.height(14.dp.scaled(sizeScale)))

        val selectable = !isReveal && effectivePick == null
        for (row in 0..1) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..1) {
                    val index = row * 2 + col
                    if (index < options.size) {
                        FlagOptionTile(
                            modifier = Modifier.weight(1f),
                            text = options[index],
                            sizeScale = sizeScale,
                            baseColor = FlagOptionColors[index % FlagOptionColors.size],
                            isReveal = isReveal,
                            isCorrect = isReveal && index == correctIndex,
                            isMySelection = index == effectivePick,
                            enabled = selectable,
                            onClick = {
                                localPick = index
                                onSelectOption(index)
                            }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (isReveal) {
            Spacer(modifier = Modifier.height(12.dp.scaled(sizeScale)))
            Text(
                text = "Next flag…",
                color = Color.LightGray,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun FlagImage(flagCode: String?, sizeScale: Float) {
    val context = LocalContext.current
    val bitmap = remember(flagCode) { flagCode?.let { loadAssetBitmap(context, "flags/$it.png") } }

    Box(
        modifier = Modifier
            .width(220.dp.scaled(sizeScale))
            .height(140.dp.scaled(sizeScale))
            .clip(RoundedCornerShape(12.dp.scaled(sizeScale)))
            .background(Color(0xFF263238))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp.scaled(sizeScale))),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = (flagCode ?: "?").uppercase(),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FlagCountdown(timerEndMillis: Long?, timerDurationSeconds: Int?) {
    var remainingSeconds by remember(timerDurationSeconds, timerEndMillis) {
        mutableIntStateOf(
            timerDurationSeconds
                ?: ((timerEndMillis ?: 0L) - System.currentTimeMillis()).div(1000L).coerceAtLeast(0L).toInt()
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

    Text(
        text = "${remainingSeconds}s",
        color = if (remainingSeconds in 1..5) Color(0xFFE53935) else Color.White,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun RevealFeedback(answered: Boolean, correct: Boolean) {
    val (label, color) = when {
        !answered -> "Time's up" to Color.LightGray
        correct -> "Correct!" to Color(0xFF66BB6A)
        else -> "Wrong" to Color(0xFFEF5350)
    }
    Text(text = label, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun FlagOptionTile(
    modifier: Modifier,
    text: String,
    sizeScale: Float,
    baseColor: Color,
    isReveal: Boolean,
    isCorrect: Boolean,
    isMySelection: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val background = when {
        isReveal && isCorrect -> FlagCorrect
        isReveal && isMySelection -> FlagWrong               // eigene falsche Wahl
        isReveal -> baseColor.copy(alpha = 0.30f)            // andere Optionen abgedunkelt
        else -> baseColor
    }
    val border = if (!isReveal && isMySelection) BorderStroke(2.dp, Color.White) else null

    Box(
        modifier = modifier
            .padding(5.dp.scaled(sizeScale))
            .height(62.dp.scaled(sizeScale))
            .clip(RoundedCornerShape(14.dp.scaled(sizeScale)))
            .background(background)
            .then(if (border != null) Modifier.border(border, RoundedCornerShape(14.dp.scaled(sizeScale))) else Modifier)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 8.dp.scaled(sizeScale)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

private fun Dp.scaled(scale: Float): Dp = this * scale

@Preview
@Composable
private fun MinigameFlagPlayingPreview() {
    Box(modifier = Modifier.background(Color(0xDD000000)).padding(24.dp)) {
        MinigameFlagRound(
            subPhase = "PLAYING",
            roundIndex = 1,
            totalRounds = 5,
            flagCode = "ar",
            options = listOf("Argentina", "Germany", "Japan", "Brazil"),
            correctName = null,
            timerEndMillis = null,
            timerDurationSeconds = 12,
            mySubmittedIndex = null,
            myScore = 1,
            onSelectOption = {}
        )
    }
}

@Preview
@Composable
private fun MinigameFlagRevealPreview() {
    Box(modifier = Modifier.background(Color(0xDD000000)).padding(24.dp)) {
        MinigameFlagRound(
            subPhase = "ROUND_REVEAL",
            roundIndex = 1,
            totalRounds = 5,
            flagCode = "ar",
            options = listOf("Argentina", "Germany", "Japan", "Brazil"),
            correctName = "Argentina",
            timerEndMillis = null,
            timerDurationSeconds = 12,
            mySubmittedIndex = 2,
            myScore = 1,
            onSelectOption = {}
        )
    }
}
