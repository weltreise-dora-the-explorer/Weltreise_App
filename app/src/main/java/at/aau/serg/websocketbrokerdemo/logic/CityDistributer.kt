package at.aau.serg.websocketbrokerdemo.logic

import at.aau.serg.websocketbrokerdemo.models.City
import at.aau.serg.websocketbrokerdemo.models.Player

class CityDistributor {

    fun distribute(allCities: List<City>, players: List<Player>, totalAmount: Int) {
        if (players.isEmpty() || allCities.isEmpty()) return

        // Wir mischen alle Städte einfach komplett durch
        val shuffledPool = allCities.shuffled().toMutableList()

        for (player in players) {
            player.ownedCities.clear()

            // Wir ziehen exakt die Anzahl an Karten, die verlangt wird
            repeat(totalAmount) {
                if (shuffledPool.isNotEmpty()) {
                    val pickedCity = shuffledPool.removeAt(0)
                    player.ownedCities.add(pickedCity)

                    // Die erste gezogene Karte ist die Startstadt
                    if (player.startCity == null) {
                        player.startCity = pickedCity
                    }
                }
            }
        }
    }
}