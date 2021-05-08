package ir.roudi.weather.data.local.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ir.roudi.weather.data.local.db.entity.City

@Dao
interface CityDao {

    @Query("SELECT * FROM city")
    fun getAllCities() : LiveData<List<City>>

    @Query("SELECT * FROM city WHERE city_id=:cityId")
    fun getCity(cityId: Int) : LiveData<City>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(city: City)

    @Update
    suspend fun updateCity(city: City)

    @Delete
    suspend fun delete(city: City)

}