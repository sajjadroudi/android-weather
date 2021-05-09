package ir.roudi.weather.ui.cities

import androidx.lifecycle.*
import ir.roudi.weather.data.Repository
import ir.roudi.weather.data.local.db.entity.City
import ir.roudi.weather.data.remote.response.City as RemoteCity
import ir.roudi.weather.data.local.pref.SharedPrefHelper
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class CitiesViewModel(
        private val repository: Repository
) : ViewModel() {

    val cities = repository.cities

    private val _actionAddNewCity = MutableLiveData(false)
    val actionAddNewCity: LiveData<Boolean>
        get() = _actionAddNewCity

    private val _selectedCityId = MutableLiveData(
        repository.getInt(SharedPrefHelper.SELECTED_CITY_ID)
    )
    val selectedCityId : LiveData<Int>
        get() = _selectedCityId

    var oldSelectedCityId : Int = 0
        private set

    fun deleteCity(city: City) {
        viewModelScope.launch {
            repository.deleteCity(city)
        }
    }

    fun insertCity(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            repository.insertCity(latitude, longitude)
        }
    }

    fun insertCity(remoteCity: RemoteCity) {
        viewModelScope.launch {
            repository.insertCity(remoteCity)
        }
    }

    fun updateCity(newCity: City) {
        viewModelScope.launch {
            repository.updateCity(newCity)
        }
    }

    suspend fun findCity(name: String) =
            repository.findCity(name)

    fun refresh() {
        viewModelScope.launch {
            repository.refresh()
        }
    }

    fun setSelectedCityId(cityId: Int) {
        oldSelectedCityId = repository.getInt(SharedPrefHelper.SELECTED_CITY_ID)
        repository.setInt(SharedPrefHelper.SELECTED_CITY_ID, cityId)
        _selectedCityId.value = cityId
    }

    fun addNewCity() {
        _actionAddNewCity.value = true
    }

    fun addNewCityCompleted() {
        _actionAddNewCity.value = false
    }

    class Factory(
            private val repository: Repository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(CitiesViewModel::class.java)) {
                return CitiesViewModel(repository) as T
            }
            throw IllegalArgumentException("Can't cast to CitiesViewModel")
        }
    }
}