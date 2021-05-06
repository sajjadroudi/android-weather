package ir.roudi.weather.ui.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ir.roudi.weather.data.Repository
import ir.roudi.weather.data.local.entity.City
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class CitiesViewModel(
        private val repository: Repository
) : ViewModel() {

    val cities = repository.cities

    fun deleteCity(city: City) {
        viewModelScope.launch {
            repository.deleteCity(city)
        }
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