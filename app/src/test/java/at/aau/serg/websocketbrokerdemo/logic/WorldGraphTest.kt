package at.aau.serg.websocketbrokerdemo.logic

import at.aau.serg.websocketbrokerdemo.models.CityColor
import at.aau.serg.websocketbrokerdemo.models.Continent
import at.aau.serg.websocketbrokerdemo.models.ConnectionType
import com.example.weltreise.logic.data.WorldGraph
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WorldGraphTest {

    private lateinit var worldGraph: WorldGraph

    // Ein kleiner Test-JSON-String, der exakt wie die json Datei aufgebaut ist.
    // Inklusive Koordinaten und kleingeschriebenen Farben zum Testen der .uppercase() Logik.
    private val testJson = """
        [
          {
            "id": "berlin",
            "name": "Berlin",
            "continent": "EUROPE",
            "color": "red",
            "trainConnections": ["paris"],
            "flightConnections": ["newyork"],
            "x": 100.0,
            "y": 100.0,
            "x_relativ": 0.1,
            "y_relativ": 0.1
          },
          {
            "id": "paris",
            "name": "Paris",
            "continent": "EUROPE",
            "color": "red",
            "trainConnections": ["berlin"],
            "flightConnections": [],
            "x": 150.0,
            "y": 150.0,
            "x_relativ": 0.15,
            "y_relativ": 0.15
          },
          {
            "id": "newyork",
            "name": "New York",
            "continent": "NORTH_AMERICA",
            "color": "orange",
            "trainConnections": [],
            "flightConnections": ["berlin"],
            "x": 200.0,
            "y": 200.0,
            "x_relativ": 0.2,
            "y_relativ": 0.2
          }
        ]
    """.trimIndent()

    @BeforeEach
    fun setup() {
        // Vor jedem Test wird ein frischer Graph mit unserem Test-JSON initialisiert
        worldGraph = WorldGraph(testJson)
    }

    @Test
    fun `test graph creates correct number of cities`() {
        assertEquals(3, worldGraph.cities.size, "Es sollten genau 3 Städte erstellt werden")
    }

    @Test
    fun `test city attributes are parsed correctly`() {
        val berlin = worldGraph.getCityById("berlin")

        assertNotNull(berlin, "Berlin sollte im Graphen existieren")
        assertEquals("Berlin", berlin?.name)
        assertEquals(Continent.EUROPE, berlin?.continent)
        // Prüft, ob aus dem "red" im JSON erfolgreich das Enum CityColor.RED wurde
        assertEquals(CityColor.RED, berlin?.color, "Die Farbe sollte korrekt als Enum erkannt werden")
    }

    @Test
    fun `test train and flight connections are set up correctly`() {
        val berlin = worldGraph.getCityById("berlin")!!
        val paris = worldGraph.getCityById("paris")!!
        val newYork = worldGraph.getCityById("newyork")!!

        // Prüfe Berlins Verbindungen
        assertEquals(2, berlin.connections.size, "Berlin sollte genau 2 Verbindungen haben")

        // Suche nach der Zugverbindung nach Paris
        val trainToParis = berlin.connections.find { it.destination == paris }
        assertNotNull(trainToParis, "Sollte eine Verbindung nach Paris haben")
        assertEquals(ConnectionType.TRAIN, trainToParis?.type, "Verbindung nach Paris sollte ein Zug sein")

        // Suche nach der Flugverbindung nach New York
        val flightToNY = berlin.connections.find { it.destination == newYork }
        assertNotNull(flightToNY, "Sollte eine Verbindung nach New York haben")
        assertEquals(ConnectionType.FLIGHT, flightToNY?.type, "Verbindung nach New York sollte ein Flug sein")
    }

    @Test
    fun `test getCityById returns null for unknown city`() {
        // Randfall testen
        val unknown = worldGraph.getCityById("wien")
        assertNull(unknown, "Eine unbekannte Stadt sollte null zurückgeben")
    }
}