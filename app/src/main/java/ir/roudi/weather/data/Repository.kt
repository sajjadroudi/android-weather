package ir.roudi.weather.data

import ir.roudi.weather.data.local.db.dao.CityDao
import ir.roudi.weather.data.local.db.dao.WeatherDao
import ir.roudi.weather.data.local.pref.SharedPrefHelper
import ir.roudi.weather.data.remote.Service
import ir.roudi.weather.data.remote.response.Weather as RemoteWeather
import ir.roudi.weather.data.local.db.entity.City as LocalCity

class Repository(
    private val cityDao: CityDao,
    private val weatherDao: WeatherDao,
    private val service: Service,
    private val sharedPref: SharedPrefHelper
) {

    val cities = cityDao.getAllCities()

    suspend fun insertCity(latitude: Double, longitude: Double) {
        val remoteCity = service.getCity(latitude, longitude)
        if(!remoteCity.isValid()) return
        cityDao.insert(remoteCity.toLocalCity())
        refresh()
    }

    suspend fun deleteCity(city: LocalCity) =
        cityDao.delete(city)

    fun getCity(cityId: Int) = cityDao.getCity(cityId)

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

    fun setInt(key: String, value: Int) =
        sharedPref.setInt(key, value)

    fun getInt(key: String, defValue: Int = 0) =
        sharedPref.getInt(key, defValue)

}