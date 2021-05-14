package ir.roudi.weather.ui.weather

import androidx.lifecycle.*
import ir.roudi.weather.data.Repository
import ir.roudi.weather.data.local.db.entity.Weather
import ir.roudi.weather.data.local.pref.SharedPrefHelper
import java.lang.IllegalArgumentException

class WeatherViewModel(
        private val repository: Repository
) : ViewModel() {

    private val selectedCityId = repository.getInt(SharedPrefHelper.SELECTED_CITY_ID)

    val existsSelectedCity = (selectedCityId != 0)

    val selectedCity = repository.getCity(selectedCityId)

    val weather = repository.getWeather(selectedCityId)

    val uiWeather : LiveData<UiWeather> = Transformations.map(weather) {
        UiWeather.from(it)
    }

    private val _actionShowMoreDetailsDialog = MutableLiveData(false)
    val actionShowMoreDetailsDialog : LiveData<Boolean>
        get() = _actionShowMoreDetailsDialog

    fun showMoreDetailsDialog() {
        _actionShowMoreDetailsDialog.value = true
    }

    fun showingMoreDetailsDialogCompleted() {
        _actionShowMoreDetailsDialog.value = false
    }

    class Factory(
            private val repository: Repository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
                return WeatherViewModel(repository) as T
            }
            throw IllegalArgumentException("Can't cast to WeatherViewModel")
        }
    }
}