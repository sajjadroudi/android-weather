package ir.roudi.weather.ui

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.roudi.weather.data.Event
import ir.roudi.weather.data.repository.Repository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val cities = repository.cities

    private val _actionShowRefresh = MutableLiveData(false)
    val actionShowRefresh : LiveData<Boolean>
        get() = _actionShowRefresh

    private val _errorMessage = MutableLiveData<Event<String?>>()
    val errorMessage : LiveData<Event<String?>>
        get() = _errorMessage

    private val _shouldUpdateWidget = MutableLiveData(Event(false))
    val shouldUpdateWidget : LiveData<Event<Boolean>>
        get() = _shouldUpdateWidget

    fun refresh(doesShowError : Boolean = false) {
        viewModelScope.launch {
            repository.refresh().collect { result ->
                if(result == null) return@collect

                _actionShowRefresh.value = result.isLoading

                if(result.isSuccessful) {
                    _shouldUpdateWidget.value = Event(true)
                } else if(result.errorOccurred && doesShowError) {
                    _errorMessage.value = Event(result.message)
                }
            }
        }
    }

}