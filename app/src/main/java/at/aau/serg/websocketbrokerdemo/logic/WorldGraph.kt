package com.example.weltreise.logic.data

import at.aau.serg.websocketbrokerdemo.models.City
import at.aau.serg.websocketbrokerdemo.models.CityData
import at.aau.serg.websocketbrokerdemo.models.ConnectionType
import at.aau.serg.websocketbrokerdemo.models.Continent

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WorldGraph(private val jsonString: String) {

    // Die Map speichert die Städte jetzt unter ihrer ID (z.B. "berlin")
    val cities = mutableMapOf<String, City>()

    init {
        initializeFromJSON()
    }

    private fun initializeFromJSON() {
        val gson = Gson()
        // Sagt Gson, dass es eine Liste von CityData-Objekten erwarten soll
        val listType = object : TypeToken<List<CityData>>() {}.type
        val cityDataList: List<CityData> = gson.fromJson(jsonString, listType)

        // DURCHLAUF 1: Alle Städte (Knoten) erschaffen
        for (data in cityDataList) {
            // String aus JSON in unser Enum umwandeln
            val continentEnum = Continent.valueOf(data.continent)
            cities[data.id] = City(data.id, data.name, continentEnum)
        }

        // DURCHLAUF 2: Die Verbindungen (Kanten) knüpfen
        for (data in cityDataList) {
            val currentCity = cities[data.id] ?: continue

            // Alle Zugverbindungen aus dem JSON auslesen
            for (targetId in data.trainConnections) {
                val targetCity = cities[targetId]
                if (targetCity != null) {
                    currentCity.addConnection(targetCity, ConnectionType.TRAIN)
                }
            }

            // Alle Flugverbindungen aus dem JSON auslesen
            for (targetId in data.flightConnections) {
                val targetCity = cities[targetId]
                if (targetCity != null) {
                    currentCity.addConnection(targetCity, ConnectionType.FLIGHT)
                }
            }
        }
    }

    fun getCityById(id: String): City? {
        return cities[id]
    }
}