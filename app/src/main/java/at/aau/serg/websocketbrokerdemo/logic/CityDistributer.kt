package at.aau.serg.websocketbrokerdemo.logic

import at.aau.serg.websocketbrokerdemo.models.City
import at.aau.serg.websocketbrokerdemo.models.Continent
import at.aau.serg.websocketbrokerdemo.models.Player

class CityDistributor {

    /**
     * Verteil-Logik: Jeder Spieler bekommt eine bestimmte Anzahl an Karten
     * pro Kontinent (Stapel), um eine faire Weltreise zu garantieren.
     * * @param allCities Die Liste aller verfügbaren Städte (ungemischt).
     * @param players Die Liste der Spieler.
     * @param amountPerContinent Wie viele Karten jeder Spieler AUS JEDEM Kontinent erhalten soll.
     */
    fun distributeByContinent(allCities: List<City>, players: List<Player>, amountPerContinent: Int) {
        if (players.isEmpty() || allCities.isEmpty()) return

        // 1. Gruppiere alle Städte nach Kontinent und mische die einzelnen Stapel
        val continentPools: Map<Continent, MutableList<City>> = allCities
            .groupBy { it.continent }
            .mapValues { (_, cities) -> cities.shuffled().toMutableList() }

        // 2. Gehe jeden Spieler durch
        for (player in players) {
            // 3. Gehe jeden Kontinent-Stapel durch
            for (continent in Continent.values()) {
                val currentPool = continentPools[continent]

                if (currentPool != null) {
                    // Ziehe n Karten für diesen Spieler aus diesem Kontinent
                    repeat(amountPerContinent) {
                        if (currentPool.isNotEmpty()) {
                            val pickedCity = currentPool.removeAt(0)
                            player.ownedCities.add(pickedCity)
                        }
                    }
                }
            }
        }
    }
}