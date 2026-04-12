package at.aau.serg.websocketbrokerdemo.logic

import at.aau.serg.websocketbrokerdemo.models.City
import at.aau.serg.websocketbrokerdemo.models.CityColor
import at.aau.serg.websocketbrokerdemo.models.Continent
import at.aau.serg.websocketbrokerdemo.models.Player
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CityDistributorTest {

    private lateinit var distributor: CityDistributor
    private lateinit var players: List<Player>
    private lateinit var testCities: List<City>

    @BeforeEach
    fun setup() {
        distributor = CityDistributor()

        players = listOf(
            Player("Alice"),
            Player("Bob"),
            Player("Charlie")
        )

        // Mock-Städte MIT dem neuen Farb-Parameter
        testCities = listOf(
            // Rot (6)
            City("wien", "Wien", Continent.EUROPE, CityColor.RED), City("berlin", "Berlin", Continent.EUROPE, CityColor.RED),
            City("paris", "Paris", Continent.EUROPE, CityColor.RED), City("rom", "Rom", Continent.EUROPE, CityColor.RED),
            City("madrid", "Madrid", Continent.EUROPE, CityColor.RED), City("london", "London", Continent.EUROPE, CityColor.RED),

            // Grün (6)
            City("tokio", "Tokio", Continent.ASIA, CityColor.GREEN), City("peking", "Peking", Continent.ASIA, CityColor.GREEN),
            City("bangkok", "Bangkok", Continent.ASIA, CityColor.GREEN), City("seoul", "Seoul", Continent.ASIA, CityColor.GREEN),
            City("neudelhi", "Neu-Delhi", Continent.ASIA, CityColor.GREEN), City("singapur", "Singapur", Continent.ASIA, CityColor.GREEN),

            // Orange (6)
            City("newyork", "New York", Continent.NORTH_AMERICA, CityColor.ORANGE), City("la", "Los Angeles", Continent.NORTH_AMERICA, CityColor.ORANGE),
            City("toronto", "Toronto", Continent.NORTH_AMERICA, CityColor.ORANGE), City("chicago", "Chicago", Continent.NORTH_AMERICA, CityColor.ORANGE),
            City("mexiko", "Mexiko-Stadt", Continent.NORTH_AMERICA, CityColor.ORANGE), City("miami", "Miami", Continent.NORTH_AMERICA, CityColor.ORANGE)
        )
    }

    @Test
    fun `distribute gives correct amount of colors and sets startCity`() {
        // Da wir im Setup nur 6 Karten pro Farbe gemockt haben,
        // lassen wir für diesen Test nur 2 Spieler spielen (brauchen exakt 6 Karten).
        val testPlayers = players.take(2)

        // Standard-Modus: Jeder zieht 3 Karten pro Farbe (insgesamt 9)
        distributor.distribute(testCities, testPlayers, 3)

        for (player in testPlayers) {
            assertEquals(9, player.ownedCities.size, "${player.name} sollte genau 9 Städte haben")

            // Test: Zählen der Farben
            val redCount = player.ownedCities.count { it.color == CityColor.RED }
            val greenCount = player.ownedCities.count { it.color == CityColor.GREEN }
            val orangeCount = player.ownedCities.count { it.color == CityColor.ORANGE }

            assertEquals(3, redCount, "${player.name} hat nicht 3 rote Karten")
            assertEquals(3, greenCount, "${player.name} hat nicht 3 grüne Karten")
            assertEquals(3, orangeCount, "${player.name} hat nicht 3 orange Karten")

            // Test: Startstadt-Logik (DoD)
            assertNotNull(player.startCity, "${player.name} hat keine Startstadt zugewiesen bekommen")
            assertTrue(player.ownedCities.contains(player.startCity), "Startstadt muss eine der gezogenen Karten sein")
        }
    }

    @Test
    fun `gracefully handles not enough cities in a pool`() {
        // Nur eine einzige Stadt im Pool
        val fewCities = listOf(City("wien", "Wien", Continent.EUROPE, CityColor.RED))

        distributor.distribute(fewCities, players, 3)

        assertEquals(1, players[0].ownedCities.size, "Alice sollte die einzige verfügbare Stadt bekommen")
        assertNotNull(players[0].startCity, "Alice sollte Wien als Startstadt haben")

        assertEquals(0, players[1].ownedCities.size, "Bob sollte leer ausgehen")
        assertNull(players[1].startCity, "Bob darf keine Startstadt haben")
    }
}