package at.aau.serg.websocketbrokerdemo

interface Callbacks {
    fun onResponse(res: String)
    fun onGoalReached(res: String)
    fun onGameOver(res: String)
    fun onConnectionLost() {}
    fun onReconnected() {}
}