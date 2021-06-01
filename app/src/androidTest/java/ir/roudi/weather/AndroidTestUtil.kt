package ir.roudi.weather

import ir.roudi.weather.data.local.db.entity.City
import ir.roudi.weather.data.local.db.entity.Weather
import java.util.*

/**
 * All generate methods of this class, generate entities with cityId starting from one
 */
object AndroidTestUtil {

    fun generateWeather(count : Int): List<Weather> {
        val list = mutableListOf<Weather>()
        for(i in 1..count) {
            list += Weather(
                    i, "main$i", "desc$i", "$i",
                    time = Calendar.getInstance(),
                    sunrise = Calendar.getInstance(),
                    sunset = Calendar.getInstance()
            )
        }
        return list
    }

    fun generateCity(count: Int): List<City> {
        val list = mutableListOf<City>()
        for(i in 1..count) {
            list += City(i, "name$i", "A", i.toDouble(), i.toDouble())
        }
        return list
    }

    fun generateOneCity() = generateCity(1)[0]

    fun generateOneWeather() = generateWeather(1)[0]

}