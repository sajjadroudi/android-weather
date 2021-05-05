package ir.roudi.weather.data.remote

import com.google.gson.*
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import ir.roudi.weather.data.remote.response.City
import ir.roudi.weather.data.remote.response.Coordinates
import ir.roudi.weather.data.remote.response.Weather
import java.lang.Exception
import java.lang.reflect.Type
import java.util.*

object GsonHelper {
    val gsonBuilder: Gson by lazy {
        GsonBuilder()
                .registerTypeAdapter(City::class.java, JsonDeserializer(::cityDeserializer))
                .registerTypeAdapter(Weather::class.java, JsonDeserializer(::weatherDeserializer))
                .setLenient()
                .create()
    }

    private val conf : Configuration by lazy {
        Configuration.defaultConfiguration()
                .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL)
    }

    private fun cityDeserializer(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) : City {

        val obj = json.asJsonObject!!

        return City(
                obj.get("id").asInt,
                obj.get("name").asString,
                read(obj.toString(), "$.sys.country") ?: "",
                context.deserialize<Coordinates>(obj.get("coord"), Coordinates::class.java)!!
        )
    }

    private fun weatherDeserializer(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?) : Weather {
        val obj = json?.asJsonObject!!
        val str = json.toString()

        return Weather(
                read(str, "$.weather[0].main")!!,
                read(str, "$.weather[0].description")!!,
                read(str, "$.weather[0].icon")!!,
                read(str, "$.main.temp")!!,
                read(str, "$.main.pressure")!!,
                read(str, "$.main.humidity")!!,
                read(str, "$.main.temp_min")!!,
                read(str, "$.main.temp_max")!!,
                read(str, "$.wind.speed")!!,
                read(str, "$.clouds.all")!!,
                read(str, "$.rain.1h"),
                read(str, "$.snow.1h"),
                Calendar.getInstance().apply { timeInMillis = obj.get("dt").asLong },
                Calendar.getInstance().apply { timeInMillis = (read<Int>(str, "$.sys.sunrise") ?: 0).toLong() },
                Calendar.getInstance().apply { timeInMillis = (read<Int>(str, "$.sys.sunset") ?: 0).toLong() },
        )
    }

    private fun <T> read(json: String, path: String): T? {
        return try {
            JsonPath.using(conf).parse(json).read(path)
        } catch (ex: Exception) {
            null
        }
    }

}