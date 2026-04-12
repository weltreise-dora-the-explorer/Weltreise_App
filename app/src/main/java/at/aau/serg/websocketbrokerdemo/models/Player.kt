package at.aau.serg.websocketbrokerdemo.models

data class Player(
    val name: String,
    var startCity: City? = null, // Ausgangsort & finales Ziel
    val ownedCities: MutableList<City> = mutableListOf(),
    val visitedCities: MutableList<City> = mutableListOf(), // Für den Fortschritt
    var currentCity: City? = null
){
    // Berechnet automatisch den Status, z.B. "3 / 9"
    val progressStatus: String
        get() = "${visitedCities.size} / ${ownedCities.size}"

    // Gibt true zurück, wenn alle Ziele erreicht sind
    val allTargetsReached: Boolean
        get() = visitedCities.size >= ownedCities.size && ownedCities.isNotEmpty()
}
