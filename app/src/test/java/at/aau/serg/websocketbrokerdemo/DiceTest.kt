package at.aau.serg.websocketbrokerdemo

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class DiceTest {
    //Prüft ob die Zahl zwischen 1 und 6 ist
    @Test
    fun diceRollReturnsValueBetween1And6() {
        val dice = Dice()

        //Mehrfahc würfeln, um sicherzustellen, dass der Wert immer im gültigen Bereich ist
        repeat(100){
            val result = dice.roll()
            assertTrue(result in 1..6)
        }
    }

    //Prüft ob nicht immer die gleiche Zahl ausgegeben wird
    @Test
    fun diceRollProducesDifferentValues(){
        val dice = Dice()
        val results = mutableSetOf<Int>()

        repeat(100){
            results.add(dice.roll())
        }
        assertTrue(results.size > 1)
    }

    //Prüft ob 1 und 6 jemals vorkommen
    @Test
    fun diceRollCanProduceMinimumAndMaximumValue() {
        val dice = Dice()
        val results = mutableSetOf<Int>()

        //Mehrfach würfeln, damit die Randwerte 1 und 6 mit hoher Wahrscheinlichkeit auftreten
        repeat(1000) {
            results.add(dice.roll())
        }

        assertTrue(results.contains(1))
        assertTrue(results.contains(6))
    }

    //Prüft ob alle Zahlen einmal vorkommen
    @Test
    fun diceRollProducesAllValuesOverManyRolls() {
        val dice = Dice()
        val results = mutableSetOf<Int>()

        repeat(2000) {
            results.add(dice.roll())
        }

        assertEquals(setOf(1,2,3,4,5,6), results)
    }

    @Test
    fun diceRollIsRoughlyUniformlyDistributed() {
        val dice = Dice()
        val counts = IntArray(6)

        repeat(10000) {
            val value = dice.roll()
            counts[value-1]++
        }

        //Bei 10000 Würfen werden pro Zahl ungefähr 1666 Treffer erwartet
        counts.forEach { count -> assertTrue(count in 1400..1900)}
    }
}