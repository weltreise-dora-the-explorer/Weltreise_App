package at.aau.serg.websocketbrokerdemo.logic

import at.aau.serg.websocketbrokerdemo.models.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CityDistributorTest {

    @Test
    fun `test distribution of exact total amount`() {
        val distributor = CityDistributor()
        val player = Player("Dora")
        // Wir geben ihm 10 Städte zur Auswahl
        val cities = List(10) { i -> City("c$i", "City $i", Continent.EUROPE, CityColor.RED) }

        // Act: Wir wollen genau 1 Karte (wie im TEST_MODE)
        distributor.distribute(cities, listOf(player), 1)

        // Assert
        assertEquals(1, player.ownedCities.size, "Es sollte genau 1 Karte verteilt werden")
        assertNotNull(player.startCity, "Startstadt muss gesetzt sein")
    }
}