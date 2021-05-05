package ir.roudi.weather.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ir.roudi.weather.data.local.entity.City

@Dao
interface CityDao {

    @Query("SELECT * FROM city")
    fun getAllCities() : LiveData<List<City>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(city: City)

    @Delete
    suspend fun delete(city: City)

}