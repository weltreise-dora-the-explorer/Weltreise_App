package at.aau.serg.websocketbrokerdemo.ui.theme.minigames.quiz

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

data class PlayerQuizResult(
    val playerId: String,
    val avatar: ImageBitmap?,
    val answerGiven: String,
    val isCorrect: Boolean,
    val timeSeconds: Float,
    val isWinner: Boolean
)

@Composable
fun QuizResultScreen(
    answers: List<String>,
    correctAnswerIndex: Int,
    submissions: Map<String, Int>,
    submissionTimes: Map<String, Long>,
    timerStartMillis: Long,
    avatars: Map<String, ImageBitmap?>,
    winnerId: String?,
    canFinishMinigame: Boolean,
    onFinishClick: () -> Unit
) {
    val context = LocalContext.current

    val mascot = remember {
        try {
            context.assets.open("baron_standing.png").use {
                BitmapFactory.decodeStream(it).asImageBitmap()
            }
        } catch (e: Exception) { null }
    }
    val crown = remember {
        try {
            context.assets.open("winner_crown.png").use {
                BitmapFactory.decodeStream(it).asImageBitmap()
            }
        } catch (e: Exception) { null }
    }

    val results = submissions.map { (id, idx) ->
        val time = (submissionTimes[id] ?: timerStartMillis) - timerStartMillis
        PlayerQuizResult(
            playerId = id,
            avatar = avatars[id],
            answerGiven = answers.getOrNull(idx) ?: "Timeout",
            isCorrect = idx == correctAnswerIndex,
            timeSeconds = Math.max(0L, time) / 1000f,
            isWinner = id == winnerId
        )
    }.sortedWith(
        compareByDescending<PlayerQuizResult> { it.isWinner }
            .thenByDescending { it.isCorrect }
            .thenBy { it.timeSeconds }
    )

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1558B5))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Results", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Seems like we have a winner....", color = Color.White, fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    results.forEach { PlayerResultRow(it, crown) }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.height(185.dp), contentAlignment = Alignment.Center) {
                        if (mascot != null) {
                            Image(bitmap = mascot, contentDescription = null,
                                contentScale = ContentScale.Fit, modifier = Modifier.fillMaxHeight(0.95f))
                        }
                    }

                    if (canFinishMinigame) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = onFinishClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF65B5FF)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .width(200.dp)
                                .height(48.dp)
                        ) {
                            Text("Let's roll", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerResultRow(result: PlayerQuizResult, crown: ImageBitmap?) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.DarkGray),
            contentAlignment = Alignment.Center) {
            if (result.avatar != null)
                Image(bitmap = result.avatar, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else
                Text(result.playerId.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFB0C4DE))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(result.playerId, color = Color(0xFF1558B5), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Answer: ${result.answerGiven}", color = Color(0xFF1558B5), fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(if (result.isCorrect) "✔️" else "❌", fontSize = 24.sp)

        Spacer(modifier = Modifier.width(8.dp))

        Text(String.format(Locale.US, "%.2fs", result.timeSeconds), color = Color.White,
            fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(58.dp),
            textAlign = TextAlign.End)

        Box(modifier = Modifier.width(32.dp), contentAlignment = Alignment.Center) {
            if (result.isWinner && crown != null)
                Image(bitmap = crown, contentDescription = "Winner", modifier = Modifier.size(22.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B213E, device = "spec:parent=pixel_7, orientation=landscape")
@Composable
fun QuizResultScreenPreview() {
    val context = LocalContext.current


    fun loadAsset(name: String): ImageBitmap? {
        return try {
            context.assets.open(name).use { BitmapFactory.decodeStream(it).asImageBitmap() }
        } catch (e: Exception) { null }
    }

    val mockAnswers = listOf("Mum Pig", "Mama Pig", "Mummy Pig", "Mother Pig")


    val avatars = mapOf(
        "Dora" to loadAsset("pp_turtle.png"),
        "Jerry" to loadAsset("pp_duck.png"),
        "Captain" to loadAsset("pp_bear.png"),
        "Leslie" to loadAsset("pp_pig.png")
    )

    val mockSubmissions = mapOf("Dora" to 2, "Jerry" to 2, "Captain" to 1, "Leslie" to 3)
    val mockTimes = mapOf("Dora" to 1005L, "Jerry" to 3360L, "Captain" to 1220L, "Leslie" to 3330L)

    QuizResultScreen(
        answers = mockAnswers,
        correctAnswerIndex = 2,
        submissions = mockSubmissions,
        submissionTimes = mockTimes,
        timerStartMillis = 0L,
        avatars = avatars,
        winnerId = "Dora",
        canFinishMinigame = true,
        onFinishClick = {}
    )
}
