package ir.roudi.weather.data.remote.response

import java.util.*

data class Weather(
    val main: String,
    val description: String,
    val iconId: String,
    val temperature: Float,
    val pressure: Int,
    val humidityPercent: Int,
    val minTemperature: Int,
    val maxTemperature: Int,
    val windSpeed: Int,
    val cloudinessPercent: Int,
    val lastHourRainVolume: Double? = null,
    val lastHourSnowVolume: Double? = null,
    val time: Calendar,
    val sunrise: Calendar,
    val sunset: Calendar
)
