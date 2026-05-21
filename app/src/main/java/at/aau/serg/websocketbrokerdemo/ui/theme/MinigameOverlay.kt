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

private enum class MinigameResultType {
    TARGET_PLAYER_WINS,
    OTHER_PLAYER_WINS
}
@Composable
fun MinigameOverlay(
    targetPlayerName: String,
    otherPlayerName: String,
    targetCityName: String,
    targetPlayerAvatar: ImageBitmap?,
    otherPlayerAvatar: ImageBitmap?,
    canFinishMinigame: Boolean,
    onFinishMinigame: (winnerPlayerId: String) -> Unit
) {
    var showVsScreen by remember { mutableStateOf(true) }
    var resultType by remember { mutableStateOf<MinigameResultType?>(null)}
    var showVisaDeniedScreen by remember { mutableStateOf(false) }

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
                showVisaDeniedScreen -> {
                    Text(
                        text = stringResource(R.string.minigame_visa_denied_title),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(
                            R.string.minigame_visa_denied_text,
                            targetPlayerName,
                            otherPlayerName
                        ),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onFinishMinigame(otherPlayerName)
                        }
                    ){
                        Text(text = stringResource(R.string.minigame_visa_denied_continue_button))
                    }
                }
                showVsScreen -> {
                    MinigamePlayerAvatar(
                        playerName = targetPlayerName,
                        avatar = targetPlayerAvatar
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.minigame_vs_title),
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    MinigamePlayerAvatar(
                        playerName = otherPlayerName,
                        avatar = otherPlayerAvatar
                    )
                }

                resultType == MinigameResultType.TARGET_PLAYER_WINS -> {
                    Text(
                        text = stringResource(R.string.minigame_result_target_wins_title),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

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

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onFinishMinigame(targetPlayerName)
                        }
                    ) {
                        Text(text = stringResource(R.string.minigame_result_continue_button))
                    }
                }

                resultType == MinigameResultType.OTHER_PLAYER_WINS -> {
                    Text(
                        text = stringResource(R.string.minigame_result_other_wins_title),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

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

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showVisaDeniedScreen = true
                        }
                    ) {
                        Text(text = stringResource(R.string.minigame_result_continue_button))
                    }
                }

                else -> {
                    Text(
                        text = stringResource(R.string.minigame_popup_title),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            resultType = MinigameResultType.TARGET_PLAYER_WINS
                        },
                        enabled = canFinishMinigame
                    ) {
                        Text(text = stringResource(R.string.minigame_target_player_wins_button))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            resultType = MinigameResultType.OTHER_PLAYER_WINS
                        },
                        enabled = canFinishMinigame
                    ) {
                        Text(text = stringResource(R.string.minigame_other_player_wins_button))
                    }
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