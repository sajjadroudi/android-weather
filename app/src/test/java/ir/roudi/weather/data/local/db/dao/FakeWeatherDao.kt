package ir.roudi.weather.data.local.db.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import ir.roudi.weather.data.local.db.entity.Weather

class FakeWeatherDao(
    private val weathers : MutableMap<Int, Weather> = mutableMapOf()
) : WeatherDao {

    override fun getWeather(cityId: Int): LiveData<Weather> {
        return liveData {
            weathers[cityId]?.let { emit(it) }
        }
    }

    override suspend fun insert(weather: Weather) {
        weathers[weather.cityId] = weather
    }

    override suspend fun insert(weathers: List<Weather>) {
        weathers.forEach { weather ->
            this.weathers[weather.cityId] = weather
        }
    }

}