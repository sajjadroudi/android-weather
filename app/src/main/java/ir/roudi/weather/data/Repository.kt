package ir.roudi.weather.data

import ir.roudi.weather.data.local.dao.CityDao
import ir.roudi.weather.data.local.dao.WeatherDao
import ir.roudi.weather.data.remote.Service
import ir.roudi.weather.data.remote.response.City as RemoteCity
import ir.roudi.weather.data.remote.response.Weather as RemoteWeather
import ir.roudi.weather.data.local.entity.City as LocalCity
import ir.roudi.weather.data.local.entity.Weather as LocalWeather

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

    suspend fun deleteCity(city: LocalCity) =
        cityDao.delete(city)

    fun getWeather(cityId: Int) = weatherDao.getWeather(cityId)

    suspend fun refresh() {
        val cities = this.cities.value ?: listOf()

        val remoteWeathers = mutableListOf<RemoteWeather>()
        cities.forEach { city ->
            val weather = service.getWeather(city.cityId)
            remoteWeathers.add(weather)
        }

        val localWeathers = remoteWeathers.toLocalWeather(cities)
        weatherDao.insert(localWeathers)
    }

}