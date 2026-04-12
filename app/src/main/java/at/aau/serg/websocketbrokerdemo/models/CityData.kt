package at.aau.serg.websocketbrokerdemo.models

data class CityData(
    val id: String,
    val name: String,
    val continent: String,
    val color: String,
    val trainConnections: List<String>,
    val flightConnections: List<String>,
    // NEU: Die Koordinaten für die UI
    val x: Double,
    val y: Double,
    val x_relativ: Double,
    val y_relativ: Double
)