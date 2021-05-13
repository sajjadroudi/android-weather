package ir.roudi.weather.data

import androidx.lifecycle.LiveData
import ir.roudi.weather.data.local.db.dao.CityDao
import ir.roudi.weather.data.local.db.dao.WeatherDao
import ir.roudi.weather.data.local.db.entity.Weather
import ir.roudi.weather.data.local.pref.SharedPrefHelper
import ir.roudi.weather.data.remote.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ir.roudi.weather.data.local.db.entity.City as LocalCity
import ir.roudi.weather.data.remote.response.City as RemoteCity
import ir.roudi.weather.data.remote.response.Weather as RemoteWeather

class Repository(
    private val cityDao: CityDao,
    private val weatherDao: WeatherDao,
    private val service: Service,
    private val sharedPref: SharedPrefHelper,
    private val coroutineScope: CoroutineScope = GlobalScope
) {

    val cities = cityDao.getAllCities()

    suspend fun insertCity(latitude: Double, longitude: Double) {
        val remoteCity = service.getCity(latitude, longitude)
        if(!remoteCity.isValid()) return
        cityDao.insert(remoteCity.toLocalCity())
    }

    suspend fun insertCity(remoteCity: RemoteCity) {
        cityDao.insert(remoteCity.toLocalCity())
    }

    suspend fun deleteCity(city: LocalCity) =
        cityDao.delete(city)

    suspend fun updateCity(city: LocalCity) =
            cityDao.updateCity(city)

    fun getCity(cityId: Int) = cityDao.getCity(cityId)

    suspend fun findCity(name: String) = service.findCity(name)

    fun getWeather(cityId: Int) = weatherDao.getWeather(cityId)

    fun fetchWeather(cityId: Int): LiveData<Weather> {
        coroutineScope.launch { refreshWeather(cityId) }
        return weatherDao.getWeather(cityId)
    }

    suspend fun refreshWeather(cityId: Int) {
        val remoteWeather = service.getWeather(cityId)
        weatherDao.insert(remoteWeather.toLocalWeather(cityId))
    }

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