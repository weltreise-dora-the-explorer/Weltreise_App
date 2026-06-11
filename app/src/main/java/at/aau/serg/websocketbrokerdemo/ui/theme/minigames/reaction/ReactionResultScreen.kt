package at.aau.serg.websocketbrokerdemo.ui.theme.minigames.reaction

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

private object ReactionResultStyle {
    val titleSize = 40.sp
    val rowWidth = 310.dp
    val rowHeight = 48.dp
    val rowShape = RoundedCornerShape(10.dp)
    val buttonWidth = 300.dp
    val buttonHeight = 54.dp
    val buttonShape = RoundedCornerShape(14.dp)

    val titleColor = Color.White
    val winnerBackground = Color(0xFFFFD95A)
    val rowBackground = Color.White
    val avatarFallbackColor = Color.DarkGray
    val primaryBlue = Color(0xFF0050A8)
    val darkText = Color(0xFF1A1A1A)
    val buttonColor = Color(0xFF0050A8)
    val avatarTextColor = Color.White
}

@Composable
fun ReactionResultScreen(
    results: List<ReactionPlayerUiState>,
    canFinishMinigame: Boolean,
    onContinueClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.reaction_result_title),
            color = ReactionResultStyle.titleColor,
            fontSize = ReactionResultStyle.titleSize,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(18.dp))

        results.forEachIndexed { index, result ->
            ReactionResultRow(
                place = index + 1,
                result = result,
                isWinner = index == 0
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onContinueClick,
            enabled = canFinishMinigame,
            colors = ButtonDefaults.buttonColors(
                containerColor = ReactionResultStyle.buttonColor
            ),
            shape = ReactionResultStyle.buttonShape,
            modifier = Modifier
                .width(ReactionResultStyle.buttonWidth)
                .height(ReactionResultStyle.buttonHeight)
        ) {
            Text(
                text = stringResource(R.string.reaction_continue_button),
                color = ReactionResultStyle.titleColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ReactionResultRow(
    place: Int,
    result: ReactionPlayerUiState,
    isWinner: Boolean
) {
    val reactionTimeSeconds = (result.reactionTimeMs ?: 0) / 1000.0

    Row(
        modifier = Modifier
            .width(ReactionResultStyle.rowWidth)
            .height(ReactionResultStyle.rowHeight)
            .background(
                color = if (isWinner)
                    ReactionResultStyle.winnerBackground
                else
                    ReactionResultStyle.rowBackground,
                shape = ReactionResultStyle.rowShape
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = place.toString(),
            color = ReactionResultStyle.darkText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ReactionResultStyle.avatarFallbackColor),
            contentAlignment = Alignment.Center
        ) {
            if (result.avatar != null) {
                Image(
                    bitmap = result.avatar,
                    contentDescription = result.playerName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = result.playerName.take(1).uppercase(),
                    color = ReactionResultStyle.avatarTextColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = result.playerName,
            color = if (isWinner)
                ReactionResultStyle.darkText
            else
                ReactionResultStyle.primaryBlue,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        if (isWinner) {
            Text(
                text = stringResource(R.string.reaction_result_winner_crown),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = stringResource(R.string.reaction_result_seconds, reactionTimeSeconds),
            color = ReactionResultStyle.primaryBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}