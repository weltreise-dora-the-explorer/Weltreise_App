package at.aau.serg.websocketbrokerdemo

import at.aau.serg.websocketbrokerdemo.logic.CityDistributor
import at.aau.serg.websocketbrokerdemo.models.City
import at.aau.serg.websocketbrokerdemo.models.Continent
import at.aau.serg.websocketbrokerdemo.models.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CityDistributorTest {

    private lateinit var distributor: CityDistributor
    private lateinit var players: List<Player>
    private lateinit var testCities: List<City>

    @BeforeEach
    fun setup() {
        distributor = CityDistributor()

        // 3 Spieler für test
        players = listOf(
            Player("Alice"),
            Player("Bob"),
            Player("Charlie")
        )

        //  Liste: 4 Kontinente mit je 6 Städten
        testCities = listOf<City>(
            // Europa (6)
            City(name = "Wien", continent = Continent.EUROPE), City(name = "Berlin", continent = Continent.EUROPE),
            City(name = "Paris", continent = Continent.EUROPE), City(name = "Rom", continent = Continent.EUROPE),
            City(name = "Madrid", continent = Continent.EUROPE), City(name = "London", continent = Continent.EUROPE),

            // Asien (6)
            City(name = "Tokio", continent = Continent.ASIA), City(name = "Peking", continent = Continent.ASIA),
            City(name = "Bangkok", continent = Continent.ASIA), City(name = "Seoul", continent = Continent.ASIA),
            City(name = "Neu-Delhi", continent = Continent.ASIA), City(name = "Singapur", continent = Continent.ASIA),

            // Nordamerika (6)
            City(name = "New York", continent = Continent.NORTH_AMERICA), City(name = "Los Angeles", continent = Continent.NORTH_AMERICA),
            City(name = "Toronto", continent = Continent.NORTH_AMERICA), City(name = "Chicago", continent = Continent.NORTH_AMERICA),
            City(name = "Mexiko-Stadt", continent = Continent.NORTH_AMERICA), City(name = "Miami", continent = Continent.NORTH_AMERICA),

            // Südamerika (6)
            City(name = "Rio de Janeiro", continent = Continent.SOUTH_AMERICA), City(name = "Buenos Aires", continent = Continent.SOUTH_AMERICA),
            City(name = "Lima", continent = Continent.SOUTH_AMERICA), City(name = "Bogota", continent = Continent.SOUTH_AMERICA),
            City(name = "Santiago", continent = Continent.SOUTH_AMERICA), City(name = "Quito", continent = Continent.SOUTH_AMERICA)
        )
    }

    @Test
    fun `test distributeByContinent gives correct amount for multiple players`() {
        // Normalfall: Jeder zieht 2 Karten pro Kontinent
        distributor.distributeByContinent(testCities, players, 2)

        for (player in players) {
            assertEquals(8, player.ownedCities.size, "${player.name} sollte genau 8 Städte haben")

            val europeCount = player.ownedCities.count { it.continent == Continent.EUROPE }
            val asiaCount = player.ownedCities.count { it.continent == Continent.ASIA }
            val naCount = player.ownedCities.count { it.continent == Continent.NORTH_AMERICA }
            val saCount = player.ownedCities.count { it.continent == Continent.SOUTH_AMERICA }

            assertEquals(2, europeCount, "${player.name} hat nicht 2 in Europa")
            assertEquals(2, asiaCount, "${player.name} hat nicht 2 in Asien")
            assertEquals(2, naCount, "${player.name} hat nicht 2 in Nordamerika")
            assertEquals(2, saCount, "${player.name} hat nicht 2 in Südamerika")
        }
    }

    @Test
    fun `test gracefully handles not enough cities in a pool`() {
        // Randfall 1: Zu wenige Städte
        val fewCities = listOf(City(name = "Wien", continent = Continent.EUROPE))

        distributor.distributeByContinent(fewCities, players, 2)

        assertEquals(1, players[0].ownedCities.size, "Alice sollte die einzige verfügbare Stadt bekommen")
        assertEquals(0, players[1].ownedCities.size, "Bob sollte leer ausgehen")
        assertEquals(0, players[2].ownedCities.size, "Charlie sollte leer ausgehen")
    }

    @Test
    fun `test handles empty city list without crashing`() {
        // Randfall 2: Gar keine Städte
        val emptyCities = emptyList<City>()

        distributor.distributeByContinent(emptyCities, players, 2)

        for (player in players) {
            assertEquals(0, player.ownedCities.size, "${player.name} sollte keine Städte haben")
        }
    }
}