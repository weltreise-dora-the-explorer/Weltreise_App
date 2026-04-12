package at.aau.serg.websocketbrokerdemo.models

data class City(
    val id: String,
    val name: String,
    val continent: Continent,
    val color: CityColor
){
    // Hier speichern wir alle abgehenden Linien (Zug oder Flug)
    val connections = mutableListOf<Connection>()

    // Eine kleine Hilfsfunktion, um leichter Verbindungen hinzuzufügen
    fun addConnection(destination: City, type: ConnectionType) {
        connections.add(Connection(destination, type))
    }
}
