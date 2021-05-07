package ir.roudi.weather.data.remote.response

import java.util.*

data class Weather(
    val main: String,
    val description: String,
    val iconId: String,
    val temperature: Float? = null,
    val pressure: Int? = null,
    val humidityPercent: Int? = null,
    val minTemperature: Int? = null,
    val maxTemperature: Int? = null,
    val windSpeed: Int? = null,
    val cloudinessPercent: Int? = null,
    val lastHourRainVolume: Double? = null,
    val lastHourSnowVolume: Double? = null,
    val time: Calendar,
    val sunrise: Calendar? = null,
    val sunset: Calendar? = null
)
