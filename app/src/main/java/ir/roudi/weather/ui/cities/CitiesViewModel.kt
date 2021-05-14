package ir.roudi.weather.ui.cities

import androidx.lifecycle.*
import ir.roudi.weather.data.Repository
import ir.roudi.weather.data.local.db.entity.City
import ir.roudi.weather.data.local.pref.SharedPrefHelper
import kotlinx.coroutines.launch
import ir.roudi.weather.data.remote.response.City as RemoteCity

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

    private val _actionShowProgressBar = MutableLiveData(false)
    val actionShowProgressBar : LiveData<Boolean>
        get() = _actionShowProgressBar

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage : LiveData<String?>
        get() = _errorMessage

    private val _shouldUpdateWidget = MutableLiveData(false)
    val shouldUpdateWidget : LiveData<Boolean>
        get() = _shouldUpdateWidget

    fun deleteCity(city: City) {
        viewModelScope.launch {
            repository.deleteCity(city)
        }

        if(city.cityId == selectedCityId.value) {
            repository.setInt(SharedPrefHelper.SELECTED_CITY_ID, 0)
            _shouldUpdateWidget.value = true
        }
    }

    fun insertCity(latitude: Double, longitude: Double) {
        handle { repository.insertCity(latitude, longitude) }
    }

    fun insertCity(remoteCity: RemoteCity) {
        handle { repository.insertCity(remoteCity) }
    }

    private fun handle(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                _actionShowProgressBar.value = true
                block()
            } catch (e: Exception) {
                _errorMessage.value = "Something went wrong"
            }
            _actionShowProgressBar.value = false
        }
    }

    fun updateCity(newCity: City) {
        viewModelScope.launch {
            repository.updateCity(newCity)
        }
    }

    suspend fun findCity(name: String) =
            repository.findCity(name)

    fun setSelectedCityId(cityId: Int) {
        oldSelectedCityId = _selectedCityId.value ?: repository.getInt(SharedPrefHelper.SELECTED_CITY_ID)
        repository.setInt(SharedPrefHelper.SELECTED_CITY_ID, cityId)
        _selectedCityId.value = cityId
        _shouldUpdateWidget.value = true
    }

    fun addNewCity() {
        _actionAddNewCity.value = true
    }

    fun addingNewCityCompleted() {
        _actionAddNewCity.value = false
    }

    fun showingErrorMessageCompleted() {
        _errorMessage.value = null
    }

    fun updatingWidgetCompleted() {
        _shouldUpdateWidget.value = false
    }

    class Factory(
            private val repository: Repository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(CitiesViewModel::class.java)) {
                return CitiesViewModel(repository) as T
            }
            throw IllegalArgumentException("Can't cast to CitiesViewModel.")
        }
    }
}