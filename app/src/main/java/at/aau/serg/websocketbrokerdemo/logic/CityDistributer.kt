package at.aau.serg.websocketbrokerdemo.logic

import at.aau.serg.websocketbrokerdemo.models.City
import at.aau.serg.websocketbrokerdemo.models.CityColor
import at.aau.serg.websocketbrokerdemo.models.Player

class CityDistributor {

    /**
     * Verteilt Städte basierend auf ihren Farben an die Spieler.
     * Die allererste gezogene Stadt wird automatisch zur Startstadt (und Ziel).
     */
    fun distribute(allCities: List<City>, players: List<Player>, amountPerColor: Int) {
        if (players.isEmpty() || allCities.isEmpty()) return

        // 1. Nach FARBE gruppieren und mischen
        val colorPools = allCities
            .groupBy { it.color }
            .mapValues { (_, cities) -> cities.shuffled().toMutableList() }

        // 2. Verteilen
        for (player in players) {
            for (color in CityColor.entries) {
                val currentPool = colorPools[color] ?: continue

                repeat(amountPerColor) {
                    if (currentPool.isNotEmpty()) {
                        val pickedCity = currentPool.removeAt(0)
                        player.ownedCities.add(pickedCity)

                        // Regel: Die erste gezogene Karte ist die Startstadt
                        if (player.startCity == null) {
                            player.startCity = pickedCity
                        }
                    }
                }
            }
        }
    }
}