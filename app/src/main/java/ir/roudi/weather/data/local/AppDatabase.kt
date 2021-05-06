package ir.roudi.weather.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ir.roudi.weather.data.local.dao.CityDao
import ir.roudi.weather.data.local.dao.WeatherDao
import ir.roudi.weather.data.local.entity.City
import ir.roudi.weather.data.local.entity.Weather

@Database(entities = [City::class, Weather::class], version = 3, exportSchema = false)
@TypeConverters(Converter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val cityDao : CityDao

    abstract val weatherDao : WeatherDao

    companion object {
        lateinit var INSTANCE : AppDatabase
        fun getInstance(context: Context): AppDatabase {
            if(!::INSTANCE.isInitialized) {
                synchronized(AppDatabase::class) {
                    val app = context.applicationContext

                    INSTANCE = Room.databaseBuilder(
                            app,
                            AppDatabase::class.java,
                            "weather.db"
                        )
                            .fallbackToDestructiveMigration()
                            .build()
                }
            }
            return INSTANCE
        }
    }

}