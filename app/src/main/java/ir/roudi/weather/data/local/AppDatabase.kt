package ir.roudi.weather.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ir.roudi.weather.data.local.entity.City
import ir.roudi.weather.data.local.entity.Weather

@Database(entities = [City::class, Weather::class], version = 1)
@TypeConverters(Converter::class)
abstract class AppDatabase : RoomDatabase() {

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