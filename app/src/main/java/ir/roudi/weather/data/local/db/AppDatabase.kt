package ir.roudi.weather.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ir.roudi.weather.data.local.db.dao.CityDao
import ir.roudi.weather.data.local.db.dao.WeatherDao
import ir.roudi.weather.data.local.db.entity.City
import ir.roudi.weather.data.local.db.entity.Weather

@Database(entities = [City::class, Weather::class], version = 5, exportSchema = false)
@TypeConverters(Converter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val cityDao : CityDao

    abstract val weatherDao : WeatherDao

}