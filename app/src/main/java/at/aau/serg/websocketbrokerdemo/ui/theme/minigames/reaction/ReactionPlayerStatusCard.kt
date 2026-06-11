package at.aau.serg.websocketbrokerdemo.ui.theme.minigames.reaction

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

private object ReactionCardStyle {
    val cardWidth = 180.dp
    val cardPaddingOuter = 6.dp
    val cardPaddingInner = 8.dp
    val avatarSize = 38.dp
    val avatarTextSpacing = 8.dp
    val cardShape = RoundedCornerShape(12.dp)

    val pressedColor = Color(0xFF35C72A)
    val readyColor = Color(0xFFD4AF37)
    val waitingColor = Color.Gray
    val cardBackgroundColor = Color.White
    val playerNameColor = Color(0xFF0050A8)
    val statusTextColor = Color.DarkGray

    val playerNameFontSize = 13.sp
    val statusFontSize = 12.sp
}

@Composable
fun ReactionPlayerStatusCard(
    player: ReactionPlayerUiState
) {
    val statusColor = when {
        player.hasPressed -> ReactionCardStyle.pressedColor
        player.isReady -> ReactionCardStyle.readyColor
        else -> ReactionCardStyle.waitingColor
    }

    val statusText = when {
        player.hasPressed -> stringResource(R.string.reaction_status_pressed)
        player.isReady -> stringResource(R.string.reaction_status_ready)
        else -> stringResource(R.string.reaction_status_waiting)
    }

    Row(
        modifier = Modifier
            .width(ReactionCardStyle.cardWidth)
            .padding(ReactionCardStyle.cardPaddingOuter)
            .background(
                color = ReactionCardStyle.cardBackgroundColor,
                shape = ReactionCardStyle.cardShape
            )
            .padding(ReactionCardStyle.cardPaddingInner),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(ReactionCardStyle.avatarSize)
                .clip(CircleShape)
                .background(statusColor),
            contentAlignment = Alignment.Center
        ) {
            if (player.avatar != null) {
                Image(
                    bitmap = player.avatar,
                    contentDescription = player.playerName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.width(ReactionCardStyle.avatarTextSpacing))

        Column {
            Text(
                text = player.playerName,
                color = ReactionCardStyle.playerNameColor,
                fontWeight = FontWeight.Bold,
                fontSize = ReactionCardStyle.playerNameFontSize
            )

            Text(
                text = statusText,
                color = ReactionCardStyle.statusTextColor,
                fontSize = ReactionCardStyle.statusFontSize
            )
        }
    }
}