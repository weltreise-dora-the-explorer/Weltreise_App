package at.aau.serg.websocketbrokerdemo.ui.theme.minigames.reaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

private object ReactionLobbyStyle {
    val titleSize = 42.sp
    val subtitleSize = 18.sp
    val hintSize = 16.sp
    val titleSpacing = 12.dp
    val playerGridSpacing = 24.dp
    val buttonShape = RoundedCornerShape(14.dp)

    val primaryTextColor = Color.White
    val buttonColor = Color(0xFF0050A8)
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

        Spacer(modifier = androidx.compose.ui.Modifier.height(ReactionLobbyStyle.titleSpacing))

        Text(
            text = stringResource(R.string.reaction_lobby_subtitle),
            color = ReactionLobbyStyle.primaryTextColor,
            fontSize = ReactionLobbyStyle.subtitleSize
        )

        Text(
            text = stringResource(R.string.reaction_lobby_ready_hint),
            color = ReactionLobbyStyle.primaryTextColor,
            fontSize = ReactionLobbyStyle.hintSize
        )

        Spacer(modifier = androidx.compose.ui.Modifier.height(ReactionLobbyStyle.playerGridSpacing))

        players.chunked(2).forEach { rowPlayers ->
            Row(horizontalArrangement = Arrangement.Center) {
                rowPlayers.forEach { player ->
                    ReactionPlayerStatusCard(player = player)
                }
            }
        }

        Spacer(modifier = androidx.compose.ui.Modifier.height(ReactionLobbyStyle.playerGridSpacing))

        Button(
            onClick = onReadyClick,
            enabled = !currentPlayerIsReady,
            colors = ButtonDefaults.buttonColors(
                containerColor = ReactionLobbyStyle.buttonColor
            ),
            shape = ReactionLobbyStyle.buttonShape
        ) {
            Text(
                text = stringResource(R.string.reaction_ready_button),
                color = ReactionLobbyStyle.primaryTextColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}