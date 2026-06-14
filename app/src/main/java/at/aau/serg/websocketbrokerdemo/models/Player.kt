package at.aau.serg.websocketbrokerdemo.models

data class Player(
    val name: String,
    // WICHTIG: Es MUSS MutableList sein, damit .add() funktioniert
    val ownedCities: MutableList<City> = mutableListOf(),
    // Wir fügen auch die startCity hinzu, da der Distributor sie gleich brauchen wird
    var startCity: City? = null
)