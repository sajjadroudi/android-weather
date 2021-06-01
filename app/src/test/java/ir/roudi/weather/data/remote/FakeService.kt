package ir.roudi.weather.data.remote

import ir.roudi.weather.data.remote.response.City
import ir.roudi.weather.data.remote.response.Coordinates
import ir.roudi.weather.data.remote.response.Weather
import java.io.IOException
import java.lang.Exception

class FakeService(
        private val cities : MutableList<City> = mutableListOf(),
        private val weathers : MutableMap<Int, Weather> = mutableMapOf()
) : Service {

    var shouldReturnNetworkError = false

    override suspend fun getCity(lat: Double, lon: Double): City {
        if(shouldReturnNetworkError)
            throw Exception("Network error")

        val city = cities.find {
            it.coordinates.latitude == lat && it.coordinates.longitude == lon
        }
        return city ?: City(0, "", "", Coordinates(0.0, 0.0))
    }

    override suspend fun findCity(name: String): City? {
        if(shouldReturnNetworkError)
            throw Exception("Network error")

        val city = cities.find { it.name == name }

        return city ?: throw Exception("No city found")
    }

    override suspend fun getWeather(cityId: Int): Weather {
        if(shouldReturnNetworkError)
            throw Exception("Network error")

        val weather = weathers[cityId]
        return weather ?: throw IOException("Not found")
    }

    fun updateWeather(cityId: Int, weather: Weather) {
        weathers[cityId] = weather
    }

}