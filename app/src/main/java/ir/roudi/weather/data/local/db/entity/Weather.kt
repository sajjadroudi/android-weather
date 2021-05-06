package ir.roudi.weather.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "weather")
data class Weather(
    @ColumnInfo(name = "city_id") @PrimaryKey val cityId: Int,
    @ColumnInfo(name = "main") val main: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "icon_id") val iconId: String,
    @ColumnInfo(name = "temperature") val temperature: Float,
    @ColumnInfo(name = "pressure") val pressure: Int,
    @ColumnInfo(name = "humidity_percent") val humidityPercent: Int,
    @ColumnInfo(name = "min_temp") val minTemperature: Int,
    @ColumnInfo(name = "max_temp") val maxTemperature: Int,
    @ColumnInfo(name = "win_speed") val windSpeed: Int,
    @ColumnInfo(name = "cloudiness_speed") val cloudinessPercent: Int,
    @ColumnInfo(name = "last_hour_rain_volume") val lastHourRainVolume: Double? = null,
    @ColumnInfo(name = "last_hour_snow_volume") val lastHourSnowVolume: Double? = null,
    @ColumnInfo(name = "time") val time: Calendar,
    @ColumnInfo(name = "sunrise") val sunrise: Calendar,
    @ColumnInfo(name = "sunset") val sunset: Calendar
)
