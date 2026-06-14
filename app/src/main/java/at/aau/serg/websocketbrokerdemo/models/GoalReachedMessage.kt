package at.aau.serg.websocketbrokerdemo.models

data class GoalReachedMessage(
    val playerName: String,
    val cityName: String,
    val reached: Int,
    val total: Int
)
