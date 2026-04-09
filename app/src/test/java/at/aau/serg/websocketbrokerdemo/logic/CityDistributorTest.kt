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

        //  Liste: 4 Kontinente mit je 6 Städten (Jetzt MIT der neuen ID als 1. Parameter)
        testCities = listOf<City>(
            // Europa (6)
            City("wien", "Wien", Continent.EUROPE), City("berlin", "Berlin", Continent.EUROPE),
            City("paris", "Paris", Continent.EUROPE), City("rom", "Rom", Continent.EUROPE),
            City("madrid", "Madrid", Continent.EUROPE), City("london", "London", Continent.EUROPE),

            // Asien (6)
            City("tokio", "Tokio", Continent.ASIA), City("peking", "Peking", Continent.ASIA),
            City("bangkok", "Bangkok", Continent.ASIA), City("seoul", "Seoul", Continent.ASIA),
            City("neudelhi", "Neu-Delhi", Continent.ASIA), City("singapur", "Singapur", Continent.ASIA),

            // Nordamerika (6)
            City("newyork", "New York", Continent.NORTH_AMERICA), City("losangeles", "Los Angeles", Continent.NORTH_AMERICA),
            City("toronto", "Toronto", Continent.NORTH_AMERICA), City("chicago", "Chicago", Continent.NORTH_AMERICA),
            City("mexikostadt", "Mexiko-Stadt", Continent.NORTH_AMERICA), City("miami", "Miami", Continent.NORTH_AMERICA),

            // Südamerika (6)
            City("rio", "Rio de Janeiro", Continent.SOUTH_AMERICA), City("buenosaires", "Buenos Aires", Continent.SOUTH_AMERICA),
            City("lima", "Lima", Continent.SOUTH_AMERICA), City("bogota", "Bogota", Continent.SOUTH_AMERICA),
            City("santiago", "Santiago", Continent.SOUTH_AMERICA), City("quito", "Quito", Continent.SOUTH_AMERICA)
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
        // Randfall 1: Zu wenige Städte (Hier auch die ID ergänzt)
        val fewCities = listOf(City("wien", "Wien", Continent.EUROPE))

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