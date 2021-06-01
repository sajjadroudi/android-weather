package ir.roudi.weather.data

import app.cash.turbine.test
import com.google.common.truth.Truth
import kotlinx.coroutines.flow.Flow
import java.util.*
import kotlin.time.ExperimentalTime
import ir.roudi.weather.data.local.db.entity.City as LocalCity
import ir.roudi.weather.data.local.db.entity.Weather as LocalWeather
import ir.roudi.weather.data.remote.response.Weather as RemoteWeather

/**
 * All generate methods of this class, generate entities with cityId starting from one
 */
object TestUtil {

    fun generateLocalWeather(count : Int): MutableList<LocalWeather> {
        val list = mutableListOf<LocalWeather>()
        for(i in 1..count) {
            list += LocalWeather(
                i, "main$i", "desc$i", "$i",
                time = Calendar.getInstance(),
                sunrise = Calendar.getInstance(),
                sunset = Calendar.getInstance()
            )
        }
        return list
    }

    fun generateLocalWeatherMap(count: Int) : MutableMap<Int, LocalWeather>
        = generateLocalWeather(count).map { it.cityId to it }.toMap().toMutableMap()

    fun generateLocalCity(count: Int): List<LocalCity> {
        val list = mutableListOf<LocalCity>()
        for(i in 1..count) {
            list += LocalCity(i, "name$i", "A", i.toDouble(), i.toDouble())
        }
        return list
    }

    fun generateLocalCityMap(count: Int) : MutableMap<Int, LocalCity>
        = generateLocalCity(count).map { it.cityId to it }.toMap().toMutableMap()

    fun generateOneLocalCity() = generateLocalCity(1)[0]

    fun generateOneLocalWeather() = generateLocalWeather(1)[0]

    fun generateRemoteCity(count: Int) = generateLocalCity(count).toRemoteCity()

    fun generateRemoteWeatherMap(count: Int) : MutableMap<Int, RemoteWeather> {
        return generateLocalWeatherMap(count).mapValues { entry ->
            entry.value.toRemoteWeather()
        }.toMutableMap()
    }

    fun generateOneRemoteCity() = generateOneLocalCity().toRemoteCity()

    @ExperimentalTime
    suspend fun <T> Flow<Result<T>?>.expectStatuses(vararg expectedStatuses : Result.Status) {
        test {
            expectedStatuses.forEach { status ->
                Truth.assertThat(expectItem()?.status).isEqualTo(status)
            }
            expectComplete()
        }
    }

    @ExperimentalTime
    suspend fun <T> Flow<Result<T>?>.expectData(data: T) {
        test {
            while(true) {
                val result = expectItem()
                if(result?.isSuccessful == true) {
                    Truth.assertThat(result.data).isEqualTo(data)
                    break
                }

                if(result?.errorOccurred == true)
                    break
            }
            cancelAndConsumeRemainingEvents()
        }
    }

}