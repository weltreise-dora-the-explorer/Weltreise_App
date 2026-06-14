package at.aau.serg.websocketbrokerdemo.ui.theme.minigames.reaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.myapplication.R

private object ReactionRoundEndedStyle {
    val titleSize = 42.sp
    val trophySize = 86.sp
    val titleTrophySpacing = 10.dp
    val trophyPlayersSpacing = 10.dp
    val playerGridRowSpacing = 8.dp

    val primaryTextColor = Color.White
}

@Composable
fun ReactionRoundEndedScreen(
    results: List<ReactionPlayerUiState>
) {
    val screenHeight = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .heightIn(max = screenHeight - 80.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.reaction_round_ended_title),
            color = ReactionRoundEndedStyle.primaryTextColor,
            fontSize = ReactionRoundEndedStyle.titleSize,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = androidx.compose.ui.Modifier.height(ReactionRoundEndedStyle.titleTrophySpacing))

        Text(
            text = stringResource(R.string.reaction_round_ended_trophy),
            fontSize = ReactionRoundEndedStyle.trophySize
        )

        Spacer(modifier = androidx.compose.ui.Modifier.height(ReactionRoundEndedStyle.trophyPlayersSpacing))

        results.chunked(2).forEach { rowPlayers ->
            Row(horizontalArrangement = Arrangement.Center) {
                rowPlayers.forEach { player ->
                    ReactionPlayerStatusCard(player = player)
                }
            }

            Spacer(modifier = androidx.compose.ui.Modifier.height(ReactionRoundEndedStyle.playerGridRowSpacing))
        }
    }
}