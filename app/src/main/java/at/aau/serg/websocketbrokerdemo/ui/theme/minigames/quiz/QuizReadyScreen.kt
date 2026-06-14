package at.aau.serg.websocketbrokerdemo.ui.theme.minigames.quiz

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun QuizReadyScreen(
    onReadyClick: () -> Unit,
    allPlayers: List<String>,
    readyPlayerIds: List<String>
) {
    val context = LocalContext.current

    val foxBitmap = remember {
        try {
            context.assets
                .open("baron_fox.png")
                .use { BitmapFactory.decodeStream(it).asImageBitmap() }
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1558B5))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fox Bild
            if (foxBitmap != null) {
                Image(
                    bitmap = foxBitmap,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )
            }

            Text(
                text = "It's Quiz Time!",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ready-Button
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF65B5FF))
                    .clickable { onReadyClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ready",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Spielerliste unten rechts
            Row(
                modifier = Modifier.align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allPlayers.forEach { name ->
                    val isReady = readyPlayerIds.contains(name)
                    val avatar = loadAvatar(name)

                    Box(contentAlignment = Alignment.BottomEnd) {
                        if (avatar != null) {
                            Image(
                                bitmap = avatar,
                                contentDescription = name,
                                modifier = Modifier.size(40.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.Gray, CircleShape)
                            )
                        }

                        // Haken-Icon
                        if (isReady) {
                            Text(
                                "✔️",
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .background(Color.White, CircleShape)
                                    .padding(2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun loadAvatar(playerName: String): ImageBitmap? {
    val context = LocalContext.current

    val fileName = when {
        playerName.contains("Dora") -> "pp_turtle.png"
        playerName.contains("Jerry") -> "pp_duck.png"
        playerName.contains("Captain") -> "pp_bear.png"
        else -> "pp_pig.png"
    }

    return remember(playerName) {
        try {
            context.assets.open(fileName).use { inputStream ->
                BitmapFactory.decodeStream(inputStream).asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }
    }
}

@Preview(showBackground = true, device = "spec:parent=pixel_7, orientation=landscape")
@Composable
fun QuizReadyScreenPreview() {
    QuizReadyScreen(onReadyClick = {}, allPlayers = listOf("Dora", "Jerry", "Captain"), readyPlayerIds = listOf("Dora"))
}