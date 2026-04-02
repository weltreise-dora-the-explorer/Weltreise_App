package at.aau.serg.websocketbrokerdemo.models

data class Player(
    val name: String,
    // Wir nutzen eine MutableList, damit wir später im Spiel
    // Städte hinzufügen oder entfernen können (z.B. durch Eroberung)
    val ownedCities: MutableList<City> = mutableListOf()
)
