package ir.roudi.weather.ui.weather

import java.text.SimpleDateFormat
import ir.roudi.weather.data.local.db.entity.Weather as LocalWeather

data class UiWeather(
    val main: String,
    val description: String,
    val iconId: String,
    val temperature: String,
    val pressure: String,
    val humidityPercent: String,
    val minTemperature: String,
    val maxTemperature: String,
    val windSpeed: String,
    val cloudinessPercent: String,
    val lastHourRainVolume: String,
    val lastHourSnowVolume: String,
    val time: String,
    val sunrise: String,
    val sunset: String
) {
    companion object {
        private fun<T> stringify(data: T?, unit: String = ""): String {
            return if(data == null) {
                "-"
            } else {
                "$data $unit"
            }
        }

        fun from(weather: LocalWeather?) : UiWeather? {
            weather ?: return null
            return weather.let {
                UiWeather(
                        it.main,
                        it.description,
                        "img_${it.iconId}",
                        stringify(it.temperature, "c"),
                        stringify(it.pressure, "hPa"),
                        stringify(it.humidityPercent, "%"),
                        stringify(it.minTemperature, "c"),
                        stringify(it.maxTemperature, "c"),
                        stringify(it.windSpeed, "m/s"),
                        stringify(it.cloudinessPercent, "%"),
                        stringify(it.lastHourRainVolume, "mm"),
                        stringify(it.lastHourSnowVolume, "mm"),
                        SimpleDateFormat("MMM d, yyyy h:mm a").format(it.time.timeInMillis),
                        SimpleDateFormat("MMM d, HH:mm:ss").format(it.sunrise?.timeInMillis),
                        SimpleDateFormat("MMM d, HH:mm:ss").format(it.sunset?.timeInMillis),
                )
            }
        }
    }
}
