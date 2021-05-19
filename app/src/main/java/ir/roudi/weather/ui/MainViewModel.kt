package ir.roudi.weather.ui

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.roudi.weather.data.Repository
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val cities = repository.cities

    private val _actionShowRefresh = MutableLiveData(false)
    val actionShowRefresh : LiveData<Boolean>
        get() = _actionShowRefresh

    private val _actionShowError = MutableLiveData(false)
    val actionShowError : LiveData<Boolean>
        get() = _actionShowError

    fun refresh(doesShowError : Boolean = false) {
        viewModelScope.launch {
            try {
                _actionShowRefresh.value = true
                repository.refresh()
            } catch (e: IOException) {
                if(doesShowError)
                    _actionShowError.value = true
            }
            _actionShowRefresh.value = false
        }
    }

    fun showingErrorCompleted() {
        _actionShowError.value = false
    }

}