package at.aau.serg.websocketbrokerdemo.logic

import at.aau.serg.websocketbrokerdemo.models.City
import at.aau.serg.websocketbrokerdemo.models.CityColor
import at.aau.serg.websocketbrokerdemo.models.Continent
import at.aau.serg.websocketbrokerdemo.models.Player
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
        // Grund-Setup für alle Tests
        berlin = City("berlin", "Berlin", Continent.EUROPE, CityColor.RED)
        paris = City("paris", "Paris", Continent.EUROPE, CityColor.RED)
        rom = City("rom", "Rom", Continent.EUROPE, CityColor.RED)

        player = Player("TestPlayer")
        player.startCity = berlin
    }

    @Test
    fun `Happy Path - Complete game flow to victory`() {
        player.ownedCities.add(paris)
        val session = GameSession(player, 1)

        // 1. Start: In Berlin, Ziel Paris noch offen
        session.visitCity(berlin)
        assertFalse(session.isVictory(), "Kein Sieg am Startpunkt ohne Ziele")

        // 2. Ziel erreichen
        session.visitCity(paris)
        assertTrue(player.visitedCities.contains(paris), "Paris sollte abgehakt sein")
        assertFalse(session.isVictory(), "Ziele erreicht, aber Rückkehr zum Start fehlt")

        // 3. Sieg
        session.visitCity(berlin)
        assertTrue(session.isVictory(), "Siegbedingung (Ziele + Startstadt) sollte erfüllt sein")
    }

    @Test
    fun `Duplicate Visit - Visiting the same target twice does not count twice`() {
        player.ownedCities.add(paris)
        val session = GameSession(player, 1)

        session.visitCity(paris)
        session.visitCity(paris) // Zweiter Besuch der gleichen Stadt

        assertEquals(1, player.visitedCities.size, "Stadt sollte nur einmal in visitedCities zählen")
    }

    @Test
    fun `Wrong Targets - Visiting cities not on owned list should not count`() {
        player.ownedCities.add(paris)
        val session = GameSession(player, 1)

        session.visitCity(rom) // Rom ist kein Pflichtziel

        assertEquals(0, player.visitedCities.size, "Nicht-Ziel-Städte dürfen nicht abgehakt werden")
        assertFalse(player.allTargetsReached, "Spieler sollte noch keine Ziele erreicht haben")
    }

    @Test
    fun `Victory Condition - Higher target count for standard mode`() {
        player.ownedCities.addAll(listOf(paris, rom))
        val session = GameSession(player, 2) // Wir verlangen 2 Ziele

        // Erstes Ziel besuchen
        session.visitCity(paris)
        session.visitCity(berlin)
        assertFalse(session.isVictory(), "Sollte nicht gewinnen, da Rom noch fehlt")

        // Zweites Ziel besuchen
        session.visitCity(rom)
        session.visitCity(berlin)
        assertTrue(session.isVictory(), "Sollte gewinnen, nachdem beide Ziele besucht wurden")
    }

    @Test
    fun `Progress Status - Correct string representation`() {
        player.ownedCities.addAll(listOf(paris, rom))
        val session = GameSession(player, 2)

        assertEquals("0 / 2", player.progressStatus)
        session.visitCity(paris)
        assertEquals("1 / 2", player.progressStatus)
        session.visitCity(rom)
        assertEquals("2 / 2", player.progressStatus)
    }
}