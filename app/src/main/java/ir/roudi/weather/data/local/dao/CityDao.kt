package ir.roudi.weather.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ir.roudi.weather.data.local.entity.City

@Dao
interface CityDao {

    @Query("SELECT * FROM city")
    fun getAllCities() : LiveData<List<City>>

    @Insert
    fun insert(city: City)

    @Delete
    fun delete(city: City)

}