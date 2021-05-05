package ir.roudi.weather.data

import ir.roudi.weather.data.local.dao.CityDao
import ir.roudi.weather.data.local.dao.WeatherDao
import ir.roudi.weather.data.remote.Service
import ir.roudi.weather.data.remote.response.Weather

class Repository(
        private val cityDao: CityDao,
        private val weatherDao: WeatherDao,
        private val service: Service
) {

    val cities = cityDao.getAllCities()

    suspend fun insertCity(latitude: Double, longitude: Double) {
        val remoteCity = service.getCity(latitude, longitude)
        cityDao.insert(remoteCity.toLocalCity())
    }

    fun getWeather(cityId: Int) = weatherDao.getWeather(cityId)

    suspend fun refresh() {
        val cities = this.cities.value ?: listOf()

        val remoteWeathers = mutableListOf<Weather>()
        cities.forEach { city ->
            val weather = service.getWeather(city.cityId)
            remoteWeathers.add(weather)
        }

        val localWeathers = remoteWeathers.toLocalWeather(cities)
        weatherDao.insert(localWeathers)
    }

}