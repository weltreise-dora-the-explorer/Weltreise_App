package at.aau.serg.websocketbrokerdemo.models

data class GameOverMessage(
    val winnerId: String,
    val results: List<PlayerResult>
) {
    data class PlayerResult(
        val playerName: String,
        val score: Int
    )
}
