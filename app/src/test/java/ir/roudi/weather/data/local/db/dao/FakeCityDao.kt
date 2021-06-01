package ir.roudi.weather.data.local.db.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import ir.roudi.weather.data.local.db.entity.City

class FakeCityDao(
    private val cities : MutableMap<Int, City> = mutableMapOf()
) : CityDao {

    private val citiesLiveData = MutableLiveData(cities.values.toList())

    override fun getAllCities(): LiveData<List<City>> {
        return citiesLiveData
    }

    override fun getCity(cityId: Int): LiveData<City> {
        return liveData { cities[cityId]?.let { emit(it) } }
    }

    override suspend fun insert(city: City) {
        cities[city.cityId] = city
        refresh()
    }

    override suspend fun updateCity(city: City) {
        cities[city.cityId] = city
        refresh()
    }

    override suspend fun delete(city: City) {
        cities.remove(city.cityId)
        refresh()
    }

    private fun refresh() {
        citiesLiveData.value = cities.values.toList()
    }
}