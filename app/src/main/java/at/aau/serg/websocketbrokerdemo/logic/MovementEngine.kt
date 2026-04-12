package at.aau.serg.websocketbrokerdemo.logic

import at.aau.serg.websocketbrokerdemo.models.City
import at.aau.serg.websocketbrokerdemo.models.Connection

class MovementEngine {

    /**
     * Ermittelt alle legalen Reiseschritte von der aktuellen Stadt aus.
     * Filtert basierend auf restlichen Würfelpunkten und Spielregeln.
     */
    fun getValidOptions(
        currentCity: City,
        previousCity: City?,
        remainingPoints: Int,
        finalDestination: City?
    ): List<Connection> {
        // Guard clause: Ohne Punkte sind keine Züge mehr möglich
        if (remainingPoints <= 0) return emptyList()

        return currentCity.connections.filter { connection ->
            // Regel: Ein unmittelbarer Rückweg im selben Zug ist verboten
            val isUTurn = connection.destination.id == previousCity?.id
            if (isUTurn) return@filter false

            val isFinalDest = connection.destination.id == finalDestination?.id
            val hasEnoughPoints = remainingPoints >= connection.type.cost

            // Die Kosten müssen gedeckt sein, außer es handelt sich um das finale Spielziel
            hasEnoughPoints || isFinalDest
        }
    }

    /**
     * Führt die Kostenabrechnung für einen ausgewählten Reiseschritt durch.
     */
    fun executeStep(
        currentPoints: Int,
        connection: Connection,
        isFinalDestination: Boolean
    ): Int {
        val newPoints = currentPoints - connection.type.cost

        // Sonderregel DoD: Erreicht der Spieler seinen finalen Zielort,
        // dürfen etwaige Restpunkte ungenutzt verfallen.
        if (isFinalDestination && newPoints < 0) {
            return 0
        }

        return newPoints
    }
}