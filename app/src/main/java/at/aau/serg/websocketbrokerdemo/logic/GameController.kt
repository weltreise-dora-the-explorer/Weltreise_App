package at.aau.serg.websocketbrokerdemo.logic

import at.aau.serg.websocketbrokerdemo.models.*

class GameController(private val worldGraph: WorldGraph) {

    private val distributor = CityDistributor()

    fun createNewSession(playerName: String, mode: GameMode): GameSession {
        val player = Player(playerName)
        val allCities = worldGraph.cities.values.toList()

        // Hier lösen wir den Fehler: Wir übergeben mode.requiredTargets (den Int)
        distributor.distribute(allCities, listOf(player), mode.requiredTargets)

        // Startposition für den Spieler setzen
        player.currentCity = player.startCity

        return GameSession(player, mode)
    }
}