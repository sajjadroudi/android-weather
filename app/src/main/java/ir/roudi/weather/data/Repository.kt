package ir.roudi.weather.data

import androidx.lifecycle.liveData
import ir.roudi.weather.data.local.db.dao.CityDao
import ir.roudi.weather.data.local.db.dao.WeatherDao
import ir.roudi.weather.data.local.pref.SharedPrefHelper
import ir.roudi.weather.data.remote.SafeApiCall
import ir.roudi.weather.data.remote.Service
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import ir.roudi.weather.data.local.db.entity.City as LocalCity
import ir.roudi.weather.data.remote.response.City as RemoteCity
import ir.roudi.weather.data.remote.response.Weather as RemoteWeather

@Singleton
class Repository @Inject constructor(
    private val cityDao: CityDao,
    private val weatherDao: WeatherDao,
    private val service: Service,
    private val sharedPref: SharedPrefHelper
) : SafeApiCall() {

    val cities = cityDao.getAllCities()

    suspend fun insertCity(latitude: Double, longitude: Double) = flow<Result<Nothing>?> {
        emit(Result.loading())

        val response = apiCall { service.getCity(latitude, longitude) }
        if(response.isSuccessful) {
            val remoteCity = response.data!!
            if (remoteCity.isValid()) {
                insertCity(remoteCity).collect { emit(it) }
            } else {
                emit(Result.error("No city found!"))
            }
        } else emit(response.toNothingWrapper())
    }

    suspend fun insertCity(remoteCity: RemoteCity) = flow<Result<Nothing>?> {
        emit(Result.loading())

        cityDao.insert(remoteCity.toLocalCity())
        emit(Result.success("City saved!"))

        refreshWeather(remoteCity.id).collect {
            if(it?.errorOccurred == true) {
                emit(Result.error("Couldn't fetch weather"))
            }
        }
    }

    suspend fun deleteCity(city: LocalCity) =
        cityDao.delete(city)

    suspend fun updateCity(city: LocalCity) =
        cityDao.updateCity(city)

    fun getCity(cityId: Int) = cityDao.getCity(cityId)

    suspend fun findCity(name: String) = flow<Result<RemoteCity?>?> {
        emit(Result.loading())
        apiCall { service.findCity(name) }
                .let { emit(it) }
    }

    fun getWeather(cityId: Int) = weatherDao.getWeather(cityId)

    fun fetchWeather(cityId: Int) = liveData {
        apiCall { service.getWeather(cityId) }
                .takeIf { it.isSuccessful }
                ?.let { weatherDao.insert(it.data!!.toLocalWeather(cityId)) }

        emitSource(weatherDao.getWeather(cityId))
    }

    private suspend fun refreshWeather(cityId: Int) = flow<Result<Nothing>?> {
        emit(Result.loading())

        apiCall { service.getWeather(cityId) }
                .also { emit(it.toNothingWrapper()) }
                .takeIf { it.isSuccessful }
                ?.let { weatherDao.insert(it.data!!.toLocalWeather(cityId)) }
    }

    suspend fun refresh() = flow<Result<Nothing>?> {
        emit(Result.loading())

        val cities = this@Repository.cities.value ?: listOf()

        val remoteWeathers = mutableListOf<RemoteWeather>()

        cities.forEach { city ->
            val response = apiCall { service.getWeather(city.cityId) }
            if(!response.isSuccessful) {
                emit(response.toNothingWrapper())
                return@flow
            }
            remoteWeathers.add(response.data!!)
        }

        remoteWeathers.toLocalWeather(cities)
                .let { weatherDao.insert(it) }

        emit(Result.success())
    }

    fun setInt(key: String, value: Int) =
        sharedPref.setInt(key, value)

    fun getInt(key: String, defValue: Int = 0) =
        sharedPref.getInt(key, defValue)

    private fun Result<*>.toNothingWrapper() : Result<Nothing> {
        return Result(status, null, message)
    }

}