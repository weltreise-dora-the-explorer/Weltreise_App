package at.aau.serg.websocketbrokerdemo.models

data class CityData(
    val id: String,
    val name: String,
    val continent: String, // Kommt als String ("EUROPE"), machen wir später zum Enum
    val trainConnections: List<String>,
    val flightConnections: List<String>
)

