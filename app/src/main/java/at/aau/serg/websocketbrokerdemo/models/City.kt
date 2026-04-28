package at.aau.serg.websocketbrokerdemo.models

data class City(
    val id: String = "",
    val name: String,
    val continent: Continent,
    val color: String = ""
)
