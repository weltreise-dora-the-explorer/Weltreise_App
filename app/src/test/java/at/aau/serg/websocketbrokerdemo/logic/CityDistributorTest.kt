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

        //  Liste: 3 Kontinente mit je 6 Städten
        testCities = listOf<City>(
            // Europa & Afrika (6)
            City(name = "Wien", continent = Continent.EUROPE_AFRICA), City(name = "Berlin", continent = Continent.EUROPE_AFRICA),
            City(name = "Paris", continent = Continent.EUROPE_AFRICA), City(name = "Rom", continent = Continent.EUROPE_AFRICA),
            City(name = "Madrid", continent = Continent.EUROPE_AFRICA), City(name = "London", continent = Continent.EUROPE_AFRICA),

            // Asien (6)
            City(name = "Tokio", continent = Continent.ASIA), City(name = "Peking", continent = Continent.ASIA),
            City(name = "Bangkok", continent = Continent.ASIA), City(name = "Seoul", continent = Continent.ASIA),
            City(name = "Neu-Delhi", continent = Continent.ASIA), City(name = "Singapur", continent = Continent.ASIA),

            // Amerika & Ozeanien (6)
            City(name = "New York", continent = Continent.AMERICAS_OCEANIA), City(name = "Los Angeles", continent = Continent.AMERICAS_OCEANIA),
            City(name = "Toronto", continent = Continent.AMERICAS_OCEANIA), City(name = "Chicago", continent = Continent.AMERICAS_OCEANIA),
            City(name = "Mexiko-Stadt", continent = Continent.AMERICAS_OCEANIA), City(name = "Miami", continent = Continent.AMERICAS_OCEANIA)
        )
    }

    @Test
    fun `test distributeByContinent gives correct amount for multiple players`() {
        // Normalfall: Jeder zieht 2 Karten pro Kontinent
        distributor.distributeByContinent(testCities, players, 2)

        for (player in players) {
            assertEquals(6, player.ownedCities.size, "${player.name} sollte genau 6 Städte haben")

            val europeAfricaCount = player.ownedCities.count { it.continent == Continent.EUROPE_AFRICA }
            val asiaCount = player.ownedCities.count { it.continent == Continent.ASIA }
            val americasCount = player.ownedCities.count { it.continent == Continent.AMERICAS_OCEANIA }

            assertEquals(2, europeAfricaCount, "${player.name} hat nicht 2 in Europa/Afrika")
            assertEquals(2, asiaCount, "${player.name} hat nicht 2 in Asien")
            assertEquals(2, americasCount, "${player.name} hat nicht 2 in Amerika/Ozeanien")
        }
    }

    @Test
    fun `test gracefully handles not enough cities in a pool`() {
        // Randfall 1: Zu wenige Städte
        val fewCities = listOf(City(name = "Wien", continent = Continent.EUROPE_AFRICA))

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