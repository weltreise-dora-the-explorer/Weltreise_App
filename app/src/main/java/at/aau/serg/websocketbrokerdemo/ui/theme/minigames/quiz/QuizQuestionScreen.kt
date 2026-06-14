package at.aau.serg.websocketbrokerdemo.ui.theme.minigames.quiz

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuizQuestionScreen(
    question: String,
    timeLeft: Int
) {
    val context = LocalContext.current

    // try-catch um Absturz zu vermeiden, falls fehlt
    val foxBitmap = remember {
        try {
            context.assets.open("baron_fox.png").use { BitmapFactory.decodeStream(it).asImageBitmap() }
        } catch (e: Exception) { null }
    }
    val questionMarksBitmap = remember {
        try {
            context.assets.open("question_marks.png").use { BitmapFactory.decodeStream(it).asImageBitmap() }
        } catch (e: Exception) { null }
    }

    //Spielbrett abdunkeln im Hintergrund
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {

        //Quiz-Box
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(350.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1558B5))
        ) {

            if (questionMarksBitmap != null) {
                Image(
                    bitmap = questionMarksBitmap,
                    contentDescription = "Question Marks",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .size(100.dp)
                )
            }

            Text(
                text = question,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 48.dp, vertical = 16.dp)
            )


            if (foxBitmap != null) {
                Image(
                    bitmap = foxBitmap,
                    contentDescription = "Baron of Brainstorm",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp)
                        .size(150.dp)
                )
            }

            //Countdown
            Text(
                text = timeLeft.toString(),
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B213E, device = "spec:parent=pixel_7, orientation=landscape")
@Composable
fun QuizQuestionScreenPreview() {
    QuizQuestionScreen(
        question = "What’s the name of\nPeppa Pig’s Mother?",
        timeLeft = 8
    )
}