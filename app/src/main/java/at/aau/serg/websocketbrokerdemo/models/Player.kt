package at.aau.serg.websocketbrokerdemo.models

data class Player(
    val name: String,
    var startCity: City? = null, // NEU: Ausgangsort & finales Ziel
    val ownedCities: MutableList<City> = mutableListOf(),
    val visitedCities: MutableList<City> = mutableListOf() // NEU: Für den Fortschritt
)
