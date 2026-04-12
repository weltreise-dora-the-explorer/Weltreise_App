package at.aau.serg.websocketbrokerdemo.logic

import at.aau.serg.websocketbrokerdemo.models.GameMode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GameControllerTest {

    @Test
    fun `test complete session initialization`() {
        // 1. Arrange: JSON mit deinen 3 Farben
        val json = """
            [
              {"id":"c1","name":"R1","continent":"EUROPE","color":"red","trainConnections":[],"flightConnections":[]},
              {"id":"c2","name":"G1","continent":"EUROPE","color":"green","trainConnections":[],"flightConnections":[]},
              {"id":"c3","name":"O1","continent":"EUROPE","color":"orange","trainConnections":[],"flightConnections":[]}
            ]
        """.trimIndent()

        val graph = WorldGraph(json)
        val controller = GameController(graph)

        // 2. Act: Erstelle eine Session im TEST_MODE (1 Ziel)
        val session = controller.createNewSession("Dora", GameMode.TEST_MODE)

        // 3. Assert
        assertNotNull(session.player.startCity, "Startstadt sollte gesetzt sein")
        assertEquals(session.player.startCity, session.player.currentCity, "Player sollte in der Startstadt stehen")

        // WICHTIG: Hier prüfen wir, ob wirklich nur 1 Karte verteilt wurde
        assertEquals(1, session.player.ownedCities.size, "Im TEST_MODE sollte genau 1 Karte verteilt werden")
        assertEquals("Dora", session.player.name)
    }
}