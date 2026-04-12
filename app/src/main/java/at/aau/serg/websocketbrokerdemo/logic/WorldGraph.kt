package at.aau.serg.websocketbrokerdemo.logic


import at.aau.serg.websocketbrokerdemo.models.City
import at.aau.serg.websocketbrokerdemo.models.CityColor
import at.aau.serg.websocketbrokerdemo.models.CityData
import at.aau.serg.websocketbrokerdemo.models.ConnectionType
import at.aau.serg.websocketbrokerdemo.models.Continent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WorldGraph(private val jsonString: String) {

    val cities = mutableMapOf<String, City>()

    init {
        initializeFromJSON()
    }

    private fun initializeFromJSON() {
        val gson = Gson()
        val listType = object : TypeToken<List<CityData>>() {}.type
        val cityDataList: List<CityData> = gson.fromJson(jsonString, listType)

        // DURCHLAUF 1: Alle Städte (Knoten) erschaffen
        for (data in cityDataList) {
            val continentEnum = Continent.valueOf(data.continent)
            val colorEnum = CityColor.valueOf(data.color.uppercase())

            cities[data.id] = City(data.id, data.name, continentEnum, colorEnum)
        }

        // DURCHLAUF 2: Die Verbindungen (Kanten) knüpfen
        for (data in cityDataList) {
            val currentCity = cities[data.id] ?: continue

            for (targetId in data.trainConnections) {
                cities[targetId]?.let { targetCity ->
                    currentCity.addConnection(targetCity, ConnectionType.TRAIN)
                }
            }

            for (targetId in data.flightConnections) {
                cities[targetId]?.let { targetCity ->
                    currentCity.addConnection(targetCity, ConnectionType.FLIGHT)
                }
            }
        }
    }

    fun getCityById(id: String): City? = cities[id]
}