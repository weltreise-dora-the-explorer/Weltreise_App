package at.aau.serg.websocketbrokerdemo // Prüfe ob das Paket stimmt!

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import at.aau.serg.websocketbrokerdemo.logic.GameController
import at.aau.serg.websocketbrokerdemo.logic.GameSession
import at.aau.serg.websocketbrokerdemo.logic.WorldGraph
import at.aau.serg.websocketbrokerdemo.models.ConnectionType
import at.aau.serg.websocketbrokerdemo.models.GameMode
import com.example.myapplication.R

class FakeConsoleActivity : Activity() {

    private lateinit var tvConsole: TextView
    private lateinit var etInput: EditText
    private lateinit var scrollView: ScrollView
    private lateinit var session: GameSession

    private var movementPoints = 0
    private var roundCounter = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fake_console)

        tvConsole = findViewById(R.id.tvConsole)
        etInput = findViewById(R.id.etInput)
        scrollView = findViewById(R.id.scrollView)

        val btnSubmit: Button = findViewById(R.id.btnSubmit)

        initGame()

        // Wenn du auf "Senden" klickst, wird das hier ausgeführt:
        btnSubmit.setOnClickListener {
            val input = etInput.text.toString().trim().lowercase()
            etInput.text.clear()
            if (input.isNotEmpty()) handleInput(input)
        }
    }

    private fun initGame() {
        printLog("=== WELTREISE: TERMINAL EDITION ===")
        // Lädt die Datei aus dem 'assets' Ordner deines Handys/Emulators
        val jsonString = assets.open("cities.json").bufferedReader().use { it.readText() }
        val worldGraph = WorldGraph(jsonString)
        val controller = GameController(worldGraph)

        session = controller.createNewSession("Tester", GameMode.CITY_HOPPER)

        printLog("Willkommen, ${session.player.name}!")
        printLog("Startstadt: ${session.player.startCity?.name}")
        printLog("Ziele: ${session.player.ownedCities.joinToString { it.name }}")

        startNewRound()
    }

    private fun startNewRound() {
        if (session.isVictory()) return
        printLog("\n--- RUNDE $roundCounter ---")
        movementPoints = (1..6).random()
        printLog("🎲 Gewürfelt: $movementPoints Punkte")
        showOptions()
    }

    private fun showOptions() {
        val current = session.player.currentCity ?: return
        printLog("\n📍 Ort: ${current.name} (Punkte: $movementPoints)")
        current.connections.forEach { conn ->
            val cost = if (conn.type == ConnectionType.FLIGHT) 2 else 1
            printLog("  - ${conn.destination.name} (${conn.type}, $cost Pkt)")
        }
        printLog("  - 'stop' (Runde beenden)")
    }

    private fun handleInput(input: String) {
        val current = session.player.currentCity ?: return
        printLog("\n> Eingabe: $input")

        if (input == "stop") {
            movementPoints = 0
            checkRoundEnd()
            return
        }

        val conn = current.connections.find { it.destination.name.lowercase() == input }
        if (conn != null) {
            val cost = if (conn.type == ConnectionType.FLIGHT) 2 else 1
            if (movementPoints >= cost) {
                movementPoints -= cost
                printLog("✅ Reise nach ${conn.destination.name} angetreten!")
                printLog(session.visitCity(conn.destination))

                if (session.isVictory()) {
                    printLog("\n🎉🎉🎉 GEWONNEN IN $roundCounter RUNDEN! 🎉🎉🎉")
                } else {
                    checkRoundEnd()
                }
            } else {
                printLog("❌ Nicht genug Punkte!")
                showOptions()
            }
        } else {
            printLog("❌ Ungültige Stadt.")
            showOptions()
        }
    }

    private fun checkRoundEnd() {
        if (movementPoints <= 0 && !session.isVictory()) {
            roundCounter++
            startNewRound()
        } else if (!session.isVictory()) {
            showOptions()
        }
    }

    private fun printLog(text: String) {
        tvConsole.append(text + "\n")
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}