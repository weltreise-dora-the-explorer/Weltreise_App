package at.aau.serg.websocketbrokerdemo.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.websocketbrokerdemo.models.GameOverMessage

private val GradientBackground = Brush.linearGradient(
    colors = listOf(Color(0xFF003058), Color(0xFFFDCB61))
)
private val Gold = Color(0xFFD4AF37)
private val Blue = Color(0xFF1E56A0)
private val LightBlue = Color(0xFF90B8D4)
private val Red = Color(0xFFB71C1C)

@Composable
fun GameOverScreen(
    currentPlayerName: String,
    results: List<GameOverMessage.PlayerResult>,
    onPlayAgainClick: () -> Unit = {},
    onLeaveClick: () -> Unit = {}
) {
    val sortedResults = results.sortedByDescending { it.score }
    val winner = sortedResults.firstOrNull()?.playerName
    val hasWon = currentPlayerName == winner

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GradientBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.width(520.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = if (hasWon) "YOU WIN!" else "YOU LOSE!",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = if (hasWon) Gold else Color.White,
                textAlign = TextAlign.Center
            )

            if (!hasWon && winner != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$winner has won the game.",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Scoreboard card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "FINAL SCORES",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gold,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Gold, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    sortedResults.forEachIndexed { index, result ->
                        val isFirst = index == 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${index + 1}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFirst) Gold else Color.Gray,
                                modifier = Modifier.width(36.dp)
                            )
                            Text(
                                text = result.playerName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blue,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${result.score} cities",
                                fontSize = 14.sp,
                                color = if (isFirst) Gold else Color.Gray,
                                fontWeight = if (isFirst) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onPlayAgainClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LightBlue)
                ) {
                    Text(
                        text = "PLAY AGAIN",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Button(
                    onClick = onLeaveClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) {
                    Text(
                        text = "LEAVE GAME",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 500)
@Composable
fun GameOverScreenPreview() {
    MaterialTheme {
        GameOverScreen(
            currentPlayerName = "Marco Polo",
            results = listOf(
                GameOverMessage.PlayerResult("DoraTheExplorer", 8),
                GameOverMessage.PlayerResult("Marco Polo", 5),
                GameOverMessage.PlayerResult("Indiana Jones", 3)
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 500)
@Composable
fun GameOverScreenWinnerPreview() {
    MaterialTheme {
        GameOverScreen(
            currentPlayerName = "DoraTheExplorer",
            results = listOf(
                GameOverMessage.PlayerResult("DoraTheExplorer", 8),
                GameOverMessage.PlayerResult("Marco Polo", 5),
                GameOverMessage.PlayerResult("Indiana Jones", 3)
            )
        )
    }
}
