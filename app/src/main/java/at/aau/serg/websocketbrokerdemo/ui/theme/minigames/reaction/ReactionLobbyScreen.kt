package at.aau.serg.websocketbrokerdemo.ui.theme.minigames.reaction

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

private object ReactionLobbyStyle {
    val titleSize = 48.sp
    val subtitleSize = 15.sp
    val hintSize = 15.sp
    val avatarSize = 48.dp
    val checkSize = 22.dp
    val titleSpacing = 26.dp
    val textSpacing = 6.dp
    val avatarSpacing = 28.dp
    val buttonTopSpacing = 24.dp
    val buttonWidth = 270.dp
    val buttonHeight = 52.dp
    val buttonShape = RoundedCornerShape(12.dp)

    val primaryTextColor = Color.White
    val buttonColor = Color(0xFF0050A8)
    val checkColor = Color(0xFF35C72A)
    val avatarFallbackColor = Color.DarkGray
}

@Composable
fun ReactionLobbyScreen(
    players: List<ReactionPlayerUiState>,
    currentPlayerName: String,
    onReadyClick: () -> Unit
) {
    val currentPlayerIsReady = players
        .firstOrNull { it.playerName == currentPlayerName }
        ?.isReady ?: false

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.reaction_minigame_title),
            color = ReactionLobbyStyle.primaryTextColor,
            fontSize = ReactionLobbyStyle.titleSize,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(ReactionLobbyStyle.titleSpacing))

        Text(
            text = stringResource(R.string.reaction_lobby_subtitle),
            color = ReactionLobbyStyle.primaryTextColor,
            fontSize = ReactionLobbyStyle.subtitleSize
        )

        Spacer(modifier = Modifier.height(ReactionLobbyStyle.textSpacing))

        Text(
            text = stringResource(R.string.reaction_lobby_ready_hint),
            color = ReactionLobbyStyle.primaryTextColor,
            fontSize = ReactionLobbyStyle.hintSize
        )

        Spacer(modifier = Modifier.height(ReactionLobbyStyle.buttonTopSpacing))

        Row(
            horizontalArrangement = Arrangement.spacedBy(ReactionLobbyStyle.avatarSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            players.forEach { player ->
                ReactionLobbyAvatar(player = player)
            }
        }

        Spacer(modifier = Modifier.height(ReactionLobbyStyle.buttonTopSpacing))

        Button(
            onClick = onReadyClick,
            enabled = !currentPlayerIsReady,
            colors = ButtonDefaults.buttonColors(
                containerColor = ReactionLobbyStyle.buttonColor,
                disabledContainerColor = ReactionLobbyStyle.buttonColor
            ),
            shape = ReactionLobbyStyle.buttonShape,
            modifier = Modifier
                .width(ReactionLobbyStyle.buttonWidth)
                .height(ReactionLobbyStyle.buttonHeight)
        ) {
            Text(
                text = stringResource(R.string.reaction_ready_button),
                color = ReactionLobbyStyle.primaryTextColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ReactionLobbyAvatar(
    player: ReactionPlayerUiState
) {
    Box(
        modifier = Modifier.size(ReactionLobbyStyle.avatarSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(ReactionLobbyStyle.avatarSize)
                .clip(CircleShape)
                .background(ReactionLobbyStyle.avatarFallbackColor),
            contentAlignment = Alignment.Center
        ) {
            if (player.avatar != null) {
                Image(
                    bitmap = player.avatar,
                    contentDescription = player.playerName,
                    modifier = Modifier.size(ReactionLobbyStyle.avatarSize),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = player.playerName.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (player.isReady) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
                    .size(ReactionLobbyStyle.checkSize)
                    .clip(CircleShape)
                    .background(ReactionLobbyStyle.checkColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}