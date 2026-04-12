package at.aau.serg.websocketbrokerdemo

import kotlin.random.Random
class Dice {
    fun roll(): Int {
        return Random.nextInt(1, 7)
    }
}