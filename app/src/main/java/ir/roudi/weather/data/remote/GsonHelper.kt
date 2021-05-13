package ir.roudi.weather.data.remote

import com.google.gson.*
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import ir.roudi.weather.data.remote.response.City
import ir.roudi.weather.data.remote.response.Coordinates
import ir.roudi.weather.data.remote.response.Weather
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

        val weather = obj.getAsJsonArray("weather")[0].asJsonObject
        val main = obj.getAsJsonObject("main")
        val windSpeed = obj.getAsJsonObject("wind")?.getAsJsonPrimitive("speed")?.asInt
        val cloudiness = obj.getAsJsonObject("clouds")?.getAsJsonPrimitive("all")?.asInt
        val rain = obj.getAsJsonObject("rain")?.getAsJsonPrimitive("1h")?.asDouble
        val snow = obj.getAsJsonObject("snow")?.getAsJsonPrimitive("1h")?.asDouble

        val sys = obj.getAsJsonObject("sys")
        val sunrise = toCalendar(sys?.getAsJsonPrimitive("sunrise")?.asLong)
        val sunset = toCalendar(sys?.getAsJsonPrimitive("sunset")?.asLong)

        return Weather(
                weather.get("main").asString,
                weather.get("description").asString,
                weather.get("icon").asString,
                main.get("temp").asFloat,
                main.get("pressure").asInt,
                main.get("humidity").asInt,
                main.get("temp_min").asInt,
                main.get("temp_max").asInt,
                windSpeed,
                cloudiness,
                rain,
                snow,
                Calendar.getInstance().apply { timeInMillis = obj.get("dt").asLong * 1000 },
                sunrise,
                sunset
        )
    }

    private fun <T> read(json: String, path: String): T? {
        return try {
            JsonPath.using(conf).parse(json).read(path)
        } catch (ex: Exception) {
            null
        }
    }

    private fun toCalendar(timeStamp: Long?): Calendar? {
        timeStamp ?: return null
        return Calendar.getInstance().apply { timeInMillis = timeStamp }
    }

}