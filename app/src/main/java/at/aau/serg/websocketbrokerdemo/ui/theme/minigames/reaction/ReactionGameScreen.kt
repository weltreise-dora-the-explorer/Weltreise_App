package at.aau.serg.websocketbrokerdemo.ui.theme.minigames.reaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

private object ReactionGameStyle {
    val titleSize = 44.sp
    val hintSize = 15.sp
    val reactionButtonSize = 120.dp
    val titleHintSpacing = 8.dp
    val buttonSpacing = 28.dp
    val playerGridRowSpacing = 8.dp

    val primaryTextColor = Color.White
    val activeButtonColor = Color(0xFF35C72A)
    val waitingButtonColor = Color(0xFF323A46)
}

@Composable
fun ReactionGameScreen(
    players: List<ReactionPlayerUiState>,
    buttonVisible: Boolean,
    onReactionClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (buttonVisible)
                stringResource(R.string.reaction_now_title)
            else
                stringResource(R.string.reaction_wait_title),
            color = ReactionGameStyle.primaryTextColor,
            fontSize = ReactionGameStyle.titleSize,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(ReactionGameStyle.titleHintSpacing))

        Text(
            text = if (buttonVisible)
                stringResource(R.string.reaction_now_hint)
            else
                stringResource(R.string.reaction_wait_hint),
            color = ReactionGameStyle.primaryTextColor,
            fontSize = ReactionGameStyle.hintSize
        )

        Spacer(modifier = Modifier.height(ReactionGameStyle.buttonSpacing))

        Box(
            modifier = Modifier
                .size(ReactionGameStyle.reactionButtonSize)
                .background(
                    color = if (buttonVisible)
                        ReactionGameStyle.activeButtonColor
                    else
                        ReactionGameStyle.waitingButtonColor,
                    shape = CircleShape
                )
                .clickable(enabled = buttonVisible) {
                    onReactionClick()
                },
            contentAlignment = Alignment.Center
        ) {
            if (buttonVisible) {
                Text(
                    text = stringResource(R.string.reaction_press_symbol),
                    color = ReactionGameStyle.primaryTextColor,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(ReactionGameStyle.buttonSpacing))

        players.chunked(2).forEach { rowPlayers ->
            Row(horizontalArrangement = Arrangement.Center) {
                rowPlayers.forEach { player ->
                    ReactionPlayerStatusCard(player = player)
                }
            }

            Spacer(modifier = Modifier.height(ReactionGameStyle.playerGridRowSpacing))
        }
    }
}