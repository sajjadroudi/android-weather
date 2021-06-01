package ir.roudi.weather.data.repository

import androidx.lifecycle.LiveData
import ir.roudi.weather.data.Result
import ir.roudi.weather.data.local.db.entity.City as LocalCity
import ir.roudi.weather.data.remote.response.City as RemoteCity
import ir.roudi.weather.data.local.db.entity.Weather
import kotlinx.coroutines.flow.Flow

interface Repository {

    val cities: LiveData<List<LocalCity>>

    suspend fun insertCity(latitude: Double, longitude: Double): Flow<Result<Nothing>?>

    suspend fun insertCity(remoteCity: RemoteCity): Flow<Result<Nothing>?>

    suspend fun deleteCity(city: LocalCity)

    suspend fun updateCity(city: LocalCity)

    fun getCity(cityId: Int): LiveData<LocalCity>

    suspend fun findCity(name: String): Flow<Result<RemoteCity?>?>

    fun getWeather(cityId: Int): LiveData<Weather>

    fun fetchWeather(cityId: Int): LiveData<Weather>

    suspend fun refresh(): Flow<Result<Nothing>?>

    fun setInt(key: String, value: Int)

    fun getInt(key: String, defValue: Int = 0): Int

}