package at.aau.serg.websocketbrokerdemo.models

/**
 * Definiert die verschiedenen Spielmodi und die jeweils
 * benötigte Anzahl an Zielkarten für einen Sieg.
 */
enum class GameMode(val requiredTargets: Int) {
    TEST_MODE(1),
    CITY_HOPPER(4),   // Schnelle Runde
    QUICK_PLAY(6),    // Mittlere Runde
    GRAND_TOUR(7),    // Lange Runde
    STANDARD(9)       // Klassisches Regelwerk
}