package at.aau.serg.websocketbrokerdemo.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import at.aau.serg.websocketbrokerdemo.ui.theme.minigames.reaction.ReactionMinigame

private enum class MinigameResultType {
    TARGET_PLAYER_WINS,
    OTHER_PLAYER_WINS
}
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
    reactionStartTimeMs: Long?,
    reactionPressTimesMs: Map<String, Long>,
    reactionButtonVisibleAtMs: Long?,
    onAnnounceMinigameResult: (winnerPlayerId: String) -> Unit,
    onReactionReady: () -> Unit,
    onReactionPress: () -> Unit,
    onFinishMinigame: (winnerPlayerId: String) -> Unit
) {
    val otherPlayerName = opponentPlayerNames.firstOrNull() ?: targetPlayerName
    val otherPlayerAvatar = opponentPlayerAvatars.firstOrNull()
    var showVsScreen by remember { mutableStateOf(true) }
    var resultType by remember { mutableStateOf<MinigameResultType?>(null)}

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

    LaunchedEffect(Unit){
        delay(2000)
        showVsScreen = false
    }

    Box(
        modifier = Modifier
            .background(Color(0xDD000000), RoundedCornerShape(20.dp))
            .padding(horizontal = 32.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){
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

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            opponentPlayerNames.forEachIndexed { index, opponentName ->
                                MinigamePlayerAvatar(
                                    playerName = opponentName,
                                    avatar = opponentPlayerAvatars.getOrNull(index)
                                )

                                if(index != opponentPlayerNames.lastIndex) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }

                resultType == MinigameResultType.TARGET_PLAYER_WINS -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ){
                        MinigamePlayerAvatar(
                            playerName = targetPlayerName,
                            avatar = targetPlayerAvatar
                        )

                        Spacer(modifier = Modifier.width(48.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Text(
                                text = stringResource(R.string.minigame_result_target_wins_title),
                                color = Color.White,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = stringResource(
                                    R.string.minigame_result_target_wins_text,
                                    targetPlayerName,
                                    targetCityName.ifBlank { "the target city" }
                                ),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(26.dp))

                            Button(
                                onClick = {
                                    onFinishMinigame(targetPlayerName)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF8DB6CD)
                                ),
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

                resultType == MinigameResultType.OTHER_PLAYER_WINS -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            opponentPlayerNames.forEachIndexed { index, opponentName ->
                                MinigamePlayerAvatar(
                                    playerName = opponentName,
                                    avatar = opponentPlayerAvatars.getOrNull(index)
                                )

                                if (index != opponentPlayerNames.lastIndex) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(48.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.minigame_result_other_wins_title),
                                color = Color.White,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = stringResource(
                                    R.string.minigame_result_other_wins_text,
                                    otherPlayerName,
                                    targetPlayerName,
                                    targetCityName.ifBlank { "the target city" }
                                ),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(26.dp))

                            Button(
                                onClick = {
                                    onFinishMinigame(otherPlayerName)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF8DB6CD)
                                ),
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

                else -> {
                    ReactionMinigame(
                        playerNames = listOf(targetPlayerName) + opponentPlayerNames,
                        playerAvatars = listOf(targetPlayerAvatar) + opponentPlayerAvatars,
                        currentPlayerName = currentPlayerName,
                        canFinishMinigame = canFinishMinigame,
                        reactionReadyPlayerIds = reactionReadyPlayerIds,
                        reactionStartTimeMs = reactionStartTimeMs,
                        reactionPressTimesMs = reactionPressTimesMs,
                        reactionButtonVisibleAtMs = reactionButtonVisibleAtMs,
                        minigameWinnerPlayerId = announcedWinnerPlayerId,
                        onReactionReady = onReactionReady,
                        onReactionPress = onReactionPress,
                        onFinishMinigame = onFinishMinigame
                    )
                }
            }
        }
    }
}

@Composable
private fun MinigamePlayerAvatar(
    playerName: String,
    avatar: ImageBitmap?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            if(avatar != null){
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