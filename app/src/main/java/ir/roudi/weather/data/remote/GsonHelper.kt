package ir.roudi.weather.data.remote

import android.util.Log
import com.google.gson.*
import com.jayway.jsonpath.JsonPath.read
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

    private fun cityDeserializer(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) : City {

        Log.i("GsonHelper", "cityDeserializer: $json")

        val jsonObject = json.asJsonObject!!

        return City(
                jsonObject.get("id").asInt,
                jsonObject.get("name").asString,
                read(jsonObject.toString(), "$.sys.country"),
                context.deserialize<Coordinates>(jsonObject.get("coord"), Coordinates::class.java)!!
        )
    }

    private fun weatherDeserializer(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?) : Weather {
        val obj = json?.asJsonObject!!
        val str = json.toString()

        return Weather(
                read(str, "$.weather.main"),
                read(str, "$.weather.description"),
                read(str, "$.weather.icon"),
                read(str, "$.main.temp"),
                read(str, "$.main.pressure"),
                read(str, "$.main.humidity"),
                read(str, "$.main.temp_min"),
                read(str, "$.main.temp_max"),
                read(str, "$.wind.speed"),
                read(str, "$.clouds.all"),
                read(str, "$.rain.1h"),
                read(str, "$.snow.1h"),
                Calendar.getInstance().apply { timeInMillis = obj.get("dt").asLong },
                Calendar.getInstance().apply { timeInMillis = read(str, "$.sys.sunrise") },
                Calendar.getInstance().apply { timeInMillis = read(str, "$.sys.sunset") },
        )
    }

}