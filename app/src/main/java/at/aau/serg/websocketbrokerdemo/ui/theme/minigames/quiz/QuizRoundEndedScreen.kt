package at.aau.serg.websocketbrokerdemo.ui.theme.minigames.quiz

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import android.graphics.BitmapFactory
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext

@Composable
fun QuizRoundEndedScreen(
    answers: List<String>,
    correctAnswerIndex: Int,
    submissions: Map<String, Int>, // Welche Spieler-ID hat welchen Antwort-Index (0-3) gewählt?
    avatars: Map<String, ImageBitmap?>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        //Quizbox
        Column(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1558B5)) //Dunkelblau
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Round ended!",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Erste Reihe Antworten (Index 0 und 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ResultQuizButton(
                    text = answers.getOrNull(0) ?: "",
                    isCorrect = correctAnswerIndex == 0,
                    playersWhoChoseThis = getPlayersForAnswer(0, submissions, avatars),
                    modifier = Modifier.weight(1f)
                )

                ResultQuizButton(
                    text = answers.getOrNull(1) ?: "",
                    isCorrect = correctAnswerIndex == 1,
                    playersWhoChoseThis = getPlayersForAnswer(1, submissions, avatars),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            //Zweite Reihe Antworten (Index 2 und 3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ResultQuizButton(
                    text = answers.getOrNull(2) ?: "",
                    isCorrect = correctAnswerIndex == 2,
                    playersWhoChoseThis = getPlayersForAnswer(2, submissions, avatars),
                    modifier = Modifier.weight(1f)
                )

                ResultQuizButton(
                    text = answers.getOrNull(3) ?: "",
                    isCorrect = correctAnswerIndex == 3,
                    playersWhoChoseThis = getPlayersForAnswer(3, submissions, avatars),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
private fun getPlayersForAnswer(
    answerIndex: Int,
    submissions: Map<String, Int>,
    avatars: Map<String, ImageBitmap?>
): List<Pair<String, ImageBitmap?>> {
    return submissions
        .filter { it.value == answerIndex }
        .map { Pair(it.key, avatars[it.key]) }
}

@Composable
private fun ResultQuizButton(
    text: String,
    isCorrect: Boolean,
    playersWhoChoseThis: List<Pair<String, ImageBitmap?>>,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isCorrect) Color(0xFF65B5FF) else Color.White.copy(alpha = 0.8f)
    val borderColor = if (isCorrect) Color(0xFFFFD579) else Color.Transparent
    val borderWidth = if (isCorrect) 3.dp else 0.dp

    Box(modifier = modifier, contentAlignment = Alignment.Center) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(borderWidth, borderColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (playersWhoChoseThis.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = (-10).dp), // Bild auf Rahmen
                horizontalArrangement = Arrangement.spacedBy((-12).dp) //kann überlappen
            ) {
                playersWhoChoseThis.forEach { (playerName, avatarBitmap) ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray)
                            .border(1.5.dp, Color.White, CircleShape)
                            .zIndex(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarBitmap != null) {
                            Image(
                                bitmap = avatarBitmap,
                                contentDescription = playerName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            //Fallback: Erster Buchstabe des Namens, falls kein Bild vorhanden ist
                            Text(
                                text = playerName.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizRoundEndedScreenPreview() {
    val context = LocalContext.current

    val duckBitmap = remember {
        try { context.assets.open("pp_duck.png").use { BitmapFactory.decodeStream(it).asImageBitmap() } } catch (e: Exception) { null }
    }
    val turtleBitmap = remember {
        try { context.assets.open("pp_turtle.png").use { BitmapFactory.decodeStream(it).asImageBitmap() } } catch (e: Exception) { null }
    }
    val bearBitmap = remember {
        try { context.assets.open("pp_bear.png").use { BitmapFactory.decodeStream(it).asImageBitmap() } } catch (e: Exception) { null }
    }
    val pigBitmap = remember {
        try { context.assets.open("pp_pig.png").use { BitmapFactory.decodeStream(it).asImageBitmap() } } catch (e: Exception) { null }
    }

    // Dummy-Daten für die Vorschau
    val mockAnswers = listOf("Mum Pig", "Mama Pig", "Mummy Pig", "Mother Pig")

    val mockSubmissions = mapOf(
        "Dora" to 2,
        "Jerry" to 2,
        "Captain" to 1,
        "Leslie" to 3
    )

    val mockAvatars = mapOf<String, ImageBitmap?>(
        "Dora" to turtleBitmap,
        "Jerry" to duckBitmap,
        "Captain" to bearBitmap,
        "Leslie" to pigBitmap
    )

    QuizRoundEndedScreen(
        answers = mockAnswers,
        correctAnswerIndex = 2,
        submissions = mockSubmissions,
        avatars = mockAvatars
    )
}

