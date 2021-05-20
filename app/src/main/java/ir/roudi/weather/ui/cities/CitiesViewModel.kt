package ir.roudi.weather.ui.cities

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.roudi.weather.data.Event
import ir.roudi.weather.data.Repository
import ir.roudi.weather.data.Result
import ir.roudi.weather.data.Result.Status.*
import ir.roudi.weather.data.local.db.entity.City
import ir.roudi.weather.data.local.pref.SharedPrefHelper
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import ir.roudi.weather.data.remote.response.City as RemoteCity

@HiltViewModel
class CitiesViewModel @Inject constructor(
        private val repository: Repository
) : ViewModel() {

    val cities = repository.cities

    private val _actionAddNewCity = MutableLiveData(Event(false))
    val actionAddNewCity: LiveData<Event<Boolean>>
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

    private val _error = MutableLiveData<Event<String>>()
    val error : LiveData<Event<String>>
        get() = _error

    private val _shouldUpdateWidget = MutableLiveData(Event(false))
    val shouldUpdateWidget : LiveData<Event<Boolean>>
        get() = _shouldUpdateWidget

    private val _message = MutableLiveData<Event<String>>()
    val message : LiveData<Event<String>>
        get() = _message

    fun deleteCity(city: City) {
        viewModelScope.launch {
            repository.deleteCity(city)
            notifyUser("Deleted!")
        }

        if(city.cityId == selectedCityId.value) {
            repository.setInt(SharedPrefHelper.SELECTED_CITY_ID, 0)
            _shouldUpdateWidget.value = Event(true)
        }
    }

    fun insertCity(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            repository.insertCity(latitude, longitude).collect(::handleInsertion)
        }
    }

    fun insertCity(remoteCity: RemoteCity) {
        viewModelScope.launch {
            repository.insertCity(remoteCity).collect(::handleInsertion)
        }
    }

    private fun<T> handleInsertion(result: Result<T>?) {
        if(result == null) return

        _actionShowProgressBar.value = result.isLoading

        when(result.status) {
            ERROR ->
                showError(result.message)
            SUCCESS ->
                notifyUser(result.message)
        }
    }

    fun updateCity(newCity: City) {
        viewModelScope.launch {
            repository.updateCity(newCity)
            notifyUser("Updated!")
        }
    }

    suspend fun findCity(name: String) =
            repository.findCity(name)

    fun setSelectedCityId(cityId: Int) {
        oldSelectedCityId = _selectedCityId.value ?: repository.getInt(SharedPrefHelper.SELECTED_CITY_ID)
        repository.setInt(SharedPrefHelper.SELECTED_CITY_ID, cityId)
        _selectedCityId.value = cityId
        _shouldUpdateWidget.value = Event(true)
    }

    fun addNewCity() {
        _actionAddNewCity.value = Event(true)
    }

    fun showError(message: String?) {
        message?.let { _error.value = Event(it) }
    }

    private fun notifyUser(message: String?) {
        message?.let { _message.value = Event(it) }
    }

}