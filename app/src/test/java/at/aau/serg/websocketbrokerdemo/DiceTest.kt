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
}