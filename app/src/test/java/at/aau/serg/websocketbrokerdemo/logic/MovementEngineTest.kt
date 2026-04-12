package at.aau.serg.websocketbrokerdemo.logic

import at.aau.serg.websocketbrokerdemo.models.City
import at.aau.serg.websocketbrokerdemo.models.CityColor
import at.aau.serg.websocketbrokerdemo.models.Continent
import at.aau.serg.websocketbrokerdemo.models.ConnectionType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MovementEngineTest {

    private lateinit var engine: MovementEngine

    // Test-Daten
    private lateinit var berlin: City
    private lateinit var paris: City
    private lateinit var newYork: City

    @BeforeEach
    fun setup() {
        engine = MovementEngine()

        // 1. Mock-Städte aufbauen
        berlin = City("berlin", "Berlin", Continent.EUROPE, CityColor.RED)
        paris = City("paris", "Paris", Continent.EUROPE, CityColor.RED)
        newYork = City("new_york", "New York", Continent.NORTH_AMERICA, CityColor.ORANGE)

        // 2. Verbindungen setzen (Berlin -> Paris: Zug, Berlin -> New York: Flug)
        berlin.addConnection(paris, ConnectionType.TRAIN)
        berlin.addConnection(newYork, ConnectionType.FLIGHT)

        paris.addConnection(berlin, ConnectionType.TRAIN)
    }

    @Test
    fun `getValidOptions should return all affordable connections`() {
        // Spieler hat 2 Punkte, beide Strecken (Kosten 1 und 2) sollten machbar sein
        val options = engine.getValidOptions(
            currentCity = berlin,
            previousCity = null,
            remainingPoints = 2,
            finalDestination = null
        )

        assertEquals(2, options.size)
        assertTrue(options.any { it.destination.id == "paris" })
        assertTrue(options.any { it.destination.id == "new_york" })
    }

    @Test
    fun `getValidOptions should filter out immediate U-Turns`() {
        // Spieler kommt von Paris nach Berlin und will weiter.
        // Paris darf NICHT mehr in den Optionen sein.
        val options = engine.getValidOptions(
            currentCity = berlin,
            previousCity = paris,
            remainingPoints = 5,
            finalDestination = null
        )

        assertEquals(1, options.size)
        assertEquals("new_york", options[0].destination.id)
    }

    @Test
    fun `getValidOptions should allow travel to final destination even if points are low`() {
        // Flug nach New York kostet 2. Spieler hat nur 1 Punkt.
        // Aber New York ist das finale Ziel, also muss es erlaubt sein!
        val options = engine.getValidOptions(
            currentCity = berlin,
            previousCity = null,
            remainingPoints = 1,
            finalDestination = newYork
        )

        assertEquals(2, options.size) // Paris (Zug=1) und New York (Zielort-Sonderregel)
        assertTrue(options.any { it.destination.id == "new_york" })
    }

    @Test
    fun `executeStep should deduct correct amount of points`() {
        val flightConnection = berlin.connections.find { it.destination.id == "new_york" }!!

        // 5 Punkte - Flug(2) = 3
        val result = engine.executeStep(5, flightConnection, isFinalDestination = false)

        assertEquals(3, result)
    }

    @Test
    fun `executeStep should let points decay to zero at final destination`() {
        val trainConnection = berlin.connections.find { it.destination.id == "paris" }!!

        // 0 Punkte - Zug(1) = -1. Da Zielort, muss es auf 0 korrigiert werden.
        val result = engine.executeStep(0, trainConnection, isFinalDestination = true)

        assertEquals(0, result)
    }
}