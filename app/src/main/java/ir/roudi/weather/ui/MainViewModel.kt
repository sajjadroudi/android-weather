package ir.roudi.weather.ui

import androidx.lifecycle.*
import ir.roudi.weather.data.Repository
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: Repository
) : ViewModel() {

    val cities = repository.cities

    private val _hasDataFetched = MutableLiveData<Boolean>()
    val hasDataFetched : LiveData<Boolean>
        get() = _hasDataFetched

    fun refresh() {
        _hasDataFetched.value = false
        viewModelScope.launch {
            repository.refresh()
            _hasDataFetched.value = true
        }
    }

    class Factory(
        private val repository: Repository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Can't cast to MainViewModel")
        }
    }

}