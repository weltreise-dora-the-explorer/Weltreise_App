package at.aau.serg.websocketbrokerdemo

import kotlin.random.Random

class Dice {
    //Gibt eine zufällige Zahl zwischen 1 und 6 zurück
    fun roll(): Int{
        return Random.nextInt(1,7)
    }
}