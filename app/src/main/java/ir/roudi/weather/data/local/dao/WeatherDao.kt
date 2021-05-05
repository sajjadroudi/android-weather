package ir.roudi.weather.data.local.dao

import androidx.room.*
import ir.roudi.weather.data.local.entity.Weather

@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather WHERE city_id=:cityId")
    fun getWeather(cityId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(weather: Weather)

    @Update
    fun update(weather: Weather)

}