package at.aau.serg.websocketbrokerdemo.logic

import at.aau.serg.websocketbrokerdemo.models.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GameSessionTest {

    private lateinit var berlin: City
    private lateinit var paris: City
    private lateinit var rom: City
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        // Setup der Test-Infrastruktur
        berlin = City("berlin", "Berlin", Continent.EUROPE, CityColor.RED)
        paris = City("paris", "Paris", Continent.EUROPE, CityColor.RED)
        rom = City("rom", "Rom", Continent.EUROPE, CityColor.RED)

        player = Player("TestPlayer")
        player.startCity = berlin
        player.currentCity = berlin
    }

    @Test
    fun `test victory in TEST_MODE with one target`() {
        player.ownedCities.add(paris)
        val session = GameSession(player, GameMode.TEST_MODE)

        session.visitCity(paris)
        session.visitCity(berlin)
        assertTrue(session.isVictory(), "Siegbedingung im TEST_MODE (1 Ziel) sollte erfüllt sein")
    }

    @Test
    fun `test CITY_HOPPER requirements`() {
        // City Hopper benötigt 4 Ziele
        val session = GameSession(player, GameMode.CITY_HOPPER)

        // Simuliere 4 Ziele
        for (i in 1..4) {
            val city = City("c$i", "City $i", Continent.EUROPE, CityColor.RED)
            player.ownedCities.add(city)
            session.visitCity(city)
        }

        assertFalse(session.isVictory(), "Noch kein Sieg ohne Rückkehr zum Start")
        session.visitCity(berlin)
        assertTrue(session.isVictory(), "Sieg nach 4 Zielen und Rückkehr")
    }

    @Test
    fun `test QUICK_PLAY and GRAND_TOUR target counts`() {
        // Diese Tests sorgen dafür, dass die Enums als 'used' markiert werden
        val quickSession = GameSession(player, GameMode.QUICK_PLAY)
        assertEquals(6, quickSession.mode.requiredTargets)

        val tourSession = GameSession(player, GameMode.GRAND_TOUR)
        assertEquals(7, tourSession.mode.requiredTargets)
    }

    @Test
    fun `test STANDARD mode full requirements`() {
        // Der Klassiker mit 9 Zielen
        val session = GameSession(player, GameMode.STANDARD)

        // Wir fügen nur 8 Ziele hinzu
        for (i in 1..8) {
            val city = City("s$i", "City $i", Continent.EUROPE, CityColor.RED)
            player.ownedCities.add(city)
            session.visitCity(city)
        }
        session.visitCity(berlin)

        assertFalse(session.isVictory(), "Im Standard-Modus reichen 8 von 9 Städten nicht aus")
    }

    @Test
    fun `test duplicate visit logic`() {
        player.ownedCities.add(paris)
        val session = GameSession(player, GameMode.TEST_MODE)

        session.visitCity(paris)
        session.visitCity(paris)

        assertEquals(1, player.visitedCities.size, "Stadt darf nur einmal in visitedCities gezählt werden")
    }

    @Test
    fun `test movement updates currentCity location`() {
        val session = GameSession(player, GameMode.TEST_MODE)

        session.visitCity(rom)
        assertEquals(rom, player.currentCity, "Die Position muss nach dem Besuch auf Rom stehen")
    }

    @Test
    fun `test feedback string icons`() {
        player.ownedCities.add(paris)
        val session = GameSession(player, GameMode.TEST_MODE)

        val targetFeedback = session.visitCity(paris)
        assertTrue(targetFeedback.contains("🎯"), "Feedback muss das Ziel-Icon enthalten")

        val neutralFeedback = session.visitCity(rom)
        assertTrue(neutralFeedback.contains("🏙️"), "Feedback muss das Stadt-Icon enthalten")
    }
}