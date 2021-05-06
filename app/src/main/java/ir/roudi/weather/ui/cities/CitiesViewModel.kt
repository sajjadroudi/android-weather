package ir.roudi.weather.ui.cities

import androidx.lifecycle.*
import ir.roudi.weather.data.Repository
import ir.roudi.weather.data.local.entity.City
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class CitiesViewModel(
        private val repository: Repository
) : ViewModel() {

    val cities = repository.cities

    private val _actionAddNewCity = MutableLiveData(false)
    val actionAddNewCity: LiveData<Boolean>
        get() = _actionAddNewCity

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