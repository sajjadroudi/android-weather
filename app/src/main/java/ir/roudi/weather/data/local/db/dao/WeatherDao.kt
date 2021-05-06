package ir.roudi.weather.data.local.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ir.roudi.weather.data.local.db.entity.Weather

@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather WHERE city_id=:cityId")
    fun getWeather(cityId: Int) : LiveData<Weather>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weather: Weather)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weathers: List<Weather>)

    @Update
    suspend fun update(weather: Weather)

}