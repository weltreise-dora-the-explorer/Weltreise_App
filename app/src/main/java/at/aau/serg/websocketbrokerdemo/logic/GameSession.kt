package at.aau.serg.websocketbrokerdemo.logic

import at.aau.serg.websocketbrokerdemo.models.City
import at.aau.serg.websocketbrokerdemo.models.GameMode
import at.aau.serg.websocketbrokerdemo.models.Player

class GameSession(
    val player: Player,
    val mode: GameMode,
) {

    /**
     * Verarbeitet das Betreten einer Stadt.
     * Aktualisiert die Spielerposition und hakt Pflichtziele in der Liste ab.
     */
    fun visitCity(city: City): String {
        player.currentCity = city

        // Prüfen, ob die Stadt ein gefordertes Ziel ist und noch nicht besucht wurde
        val isTarget = player.ownedCities.any { it.id == city.id }
        val alreadyVisited = player.visitedCities.any { it.id == city.id }

        return when {
            isTarget && !alreadyVisited -> {
                player.visitedCities.add(city)
                "🎯 Ziel erreicht: ${city.name}! Stand: ${player.progressStatus}"
            }
            isTarget && alreadyVisited -> {
                "📍 Bereits abgehakt: ${city.name}"
            }
            else -> {
                "🏙️ Zwischenstopp: ${city.name}"
            }
        }
    }

    /**
     * Überprüft die Siegbedingungen:
     * 1. Alle benötigten Pflichtziele müssen in visitedCities sein.
     * 2. Die aktuelle Position muss der startCity entsprechen.
     */
    fun isVictory(): Boolean {
        // Wir greifen auf den Wert im Enum zu
        val allTargetsDone = player.visitedCities.size >= mode.requiredTargets
        val backAtHome = player.currentCity?.id == player.startCity?.id

        return allTargetsDone && backAtHome
    }
}