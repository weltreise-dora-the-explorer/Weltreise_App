package at.aau.serg.websocketbrokerdemo.models

data class City(
    val id: String = "",
    val name: String,
    val continent: Continent,
    val color: String = "",
    val x_relativ: Float = 0f,
    val y_relativ: Float = 0f,
    val trainConnections: List<String> = emptyList(),
    val flightConnections: List<String> = emptyList()
)
