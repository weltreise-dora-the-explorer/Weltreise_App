package at.aau.serg.websocketbrokerdemo.ui.theme.minigames.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview


private object QuizGameStyle {
    val questionSize = 22.sp
    val answerSize = 18.sp
    val cardPadding = 24.dp
    val buttonSpacing = 16.dp
    val buttonHeight = 60.dp
    val cornerRadius = 16.dp

    val textColor = Color.White
    val selectedTextColor = Color.Black


    val cardBackgroundColor = Color(0xFF1558B5) // Dunkelblau
    val defaultButtonColor = Color(0xFF65B5FF)  // Hellblau
    val selectedButtonColor = Color(0xFFFFD579) // Gelb

    val timerHeight = 20.dp
    val timerGreen = Color(0xFF6ED050)
    val timerYellow = Color(0xFFFFD579)
    val timerRed = Color(0xFFFF4B4B)
    val timerTrackColor = Color.White
}
@Composable
fun QuizGameScreen(
    state: QuizUiState,
    onAnswerClick: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // blaue Box
        Column(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .clip(RoundedCornerShape(QuizGameStyle.cornerRadius))
                .background(QuizGameStyle.cardBackgroundColor)
                .padding(QuizGameStyle.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = state.question,
                color = QuizGameStyle.textColor,
                fontSize = QuizGameStyle.questionSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Erste Reihe Antworten (Index 0 und 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(QuizGameStyle.buttonSpacing)
            ) {
                QuizButton(
                    text = state.answers.getOrNull(0) ?: "",
                    isSelected = state.selectedAnswer == state.answers.getOrNull(0),
                    modifier = Modifier.weight(1f)
                ) { onAnswerClick(state.answers.getOrNull(0) ?: "") }

                QuizButton(
                    text = state.answers.getOrNull(1) ?: "",
                    isSelected = state.selectedAnswer == state.answers.getOrNull(1),
                    modifier = Modifier.weight(1f)
                ) { onAnswerClick(state.answers.getOrNull(1) ?: "") }
            }

            Spacer(modifier = Modifier.height(QuizGameStyle.buttonSpacing))

            // Zweite Reihe Antworten (Index 2 und 3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(QuizGameStyle.buttonSpacing)
            ) {
                QuizButton(
                    text = state.answers.getOrNull(2) ?: "",
                    isSelected = state.selectedAnswer == state.answers.getOrNull(2),
                    modifier = Modifier.weight(1f)
                ) { onAnswerClick(state.answers.getOrNull(2) ?: "") }

                QuizButton(
                    text = state.answers.getOrNull(3) ?: "",
                    isSelected = state.selectedAnswer == state.answers.getOrNull(3),
                    modifier = Modifier.weight(1f)
                ) { onAnswerClick(state.answers.getOrNull(3) ?: "") }
                }

                //Ladebalken für Timer
                if (state.isTimerActive) {
                    Spacer(modifier = Modifier.height(32.dp))

                    val currentTimerColor = when {
                        state.timeLeftProgress > 0.5f -> QuizGameStyle.timerGreen
                        state.timeLeftProgress > 0.2f -> QuizGameStyle.timerYellow
                        else -> QuizGameStyle.timerRed
                    }

                    Box( modifier = Modifier .fillMaxWidth(0.8f)
                        .height(QuizGameStyle.timerHeight)
                        .clip(RoundedCornerShape(50))
                        .background(QuizGameStyle.timerTrackColor),
                        contentAlignment = Alignment.CenterStart
                    )
                    {
                        Box( modifier = Modifier .fillMaxWidth(state.timeLeftProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(currentTimerColor)
                        )
                    }
            }
        }
    }
}
@Composable
private fun QuizButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) QuizGameStyle.selectedButtonColor else QuizGameStyle.defaultButtonColor
    val textColor = if (isSelected) QuizGameStyle.selectedTextColor else QuizGameStyle.textColor

    Box(
        modifier = modifier
            .height(QuizGameStyle.buttonHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = QuizGameStyle.answerSize,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B213E, device = "spec:parent=pixel_7, orientation=landscape")
@Composable
fun QuizGameScreenPreview() {
    QuizGameScreen(
        state = QuizUiState(
            question = "What’s the name of Peppa Pig’s Mother?",
            answers = listOf("Mum Pig", "Mama Pig", "Mummy Pig", "Mother Pig"),
            selectedAnswer = null,
            isTimerActive = true,
            timeLeftProgress = 0.3f
        ),
        onAnswerClick = {}
    )
}