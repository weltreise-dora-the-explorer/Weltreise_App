package at.aau.serg.websocketbrokerdemo

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class DiceTest {
    @Test
    fun diceRollReturnsValueBetween1And6() {
        val dice = Dice()

        repeat(100){
            val result = dice.roll()
            assertTrue(result in 1..6)
        }
    }

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