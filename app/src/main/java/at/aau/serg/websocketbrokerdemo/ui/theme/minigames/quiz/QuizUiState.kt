package at.aau.serg.websocketbrokerdemo.ui.theme.minigames.quiz


 // Zustand des Quiz-Bildschirms zu jedem ZP
data class QuizUiState(
    val question: String = "",
    val answers: List<String> = emptyList(),
    val selectedAnswer: String? = null,
    val isTimerActive: Boolean = false,
    val timeLeftProgress: Float = 1.0f //Ladebalken (1.0 = voll, 0.0 = leer)
)