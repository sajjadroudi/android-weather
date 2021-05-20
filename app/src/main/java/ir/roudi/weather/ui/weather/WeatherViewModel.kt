package ir.roudi.weather.ui.weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.roudi.weather.data.Event
import ir.roudi.weather.data.Repository
import ir.roudi.weather.data.local.pref.SharedPrefHelper
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
        private val repository: Repository
) : ViewModel() {

    private val selectedCityId = repository.getInt(SharedPrefHelper.SELECTED_CITY_ID)

    val existsSelectedCity = (selectedCityId != 0)

    val selectedCity = repository.getCity(selectedCityId)

    val weather = repository.getWeather(selectedCityId)

    val uiWeather : LiveData<UiWeather> = Transformations.map(weather) {
        UiWeather.from(it)
    }

    private val _actionShowMoreDetailsDialog = MutableLiveData(Event(false))
    val actionShowMoreDetailsDialog : LiveData<Event<Boolean>>
        get() = _actionShowMoreDetailsDialog

    fun showMoreDetailsDialog() {
        _actionShowMoreDetailsDialog.value = Event(true)
    }

}