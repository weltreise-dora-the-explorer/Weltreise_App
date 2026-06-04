package at.aau.serg.websocketbrokerdemo.ui.theme.minigames.reaction

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

private object ReactionCountdownStyle {
    val titleSize = 36.sp
    val hintSize = 16.sp
    val countdownSize = 180.dp
    val countdownTextSize = 72.sp
    val titleHintSpacing = 14.dp
    val countdownSpacing = 24.dp
    val playerGridRowSpacing = 8.dp
    val countdownRingWidth = 12.dp
    val countdownAnimationDurationMs = 800

    val primaryTextColor = Color.White
    val ringBackgroundColor = Color(0xFF2A3440)
    val ringProgressColor = Color(0xFF35C7FF)
}

@Composable
fun ReactionCountdownScreen(
    countdownValue: Int,
    players: List<ReactionPlayerUiState>
) {
    val ringProgress = remember { Animatable(1f) }

    LaunchedEffect(countdownValue) {
        ringProgress.snapTo(1f)
        ringProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = ReactionCountdownStyle.countdownAnimationDurationMs,
                easing = LinearEasing
            )
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.reaction_game_title),
            color = ReactionCountdownStyle.primaryTextColor,
            fontSize = ReactionCountdownStyle.titleSize,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(ReactionCountdownStyle.titleHintSpacing))

        Text(
            text = stringResource(R.string.reaction_countdown_hint),
            color = ReactionCountdownStyle.primaryTextColor,
            fontSize = ReactionCountdownStyle.hintSize
        )

        Spacer(modifier = Modifier.height(ReactionCountdownStyle.countdownSpacing))

        Box(
            modifier = Modifier.size(ReactionCountdownStyle.countdownSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.size(ReactionCountdownStyle.countdownSize)
            ) {
                drawCircle(
                    color = ReactionCountdownStyle.ringBackgroundColor,
                    style = Stroke(
                        width = ReactionCountdownStyle.countdownRingWidth.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                drawArc(
                    color = ReactionCountdownStyle.ringProgressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * ringProgress.value,
                    useCenter = false,
                    style = Stroke(
                        width = ReactionCountdownStyle.countdownRingWidth.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }

            Text(
                text = countdownValue.toString(),
                color = ReactionCountdownStyle.primaryTextColor,
                fontSize = ReactionCountdownStyle.countdownTextSize,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(ReactionCountdownStyle.countdownSpacing))

        players.chunked(2).forEach { rowPlayers ->
            Row(horizontalArrangement = Arrangement.Center) {
                rowPlayers.forEach { player ->
                    ReactionPlayerStatusCard(player = player)
                }
            }

            Spacer(modifier = Modifier.height(ReactionCountdownStyle.playerGridRowSpacing))
        }
    }
}