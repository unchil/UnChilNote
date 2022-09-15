package com.example.unchilnote.weather

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.ListFragment
import androidx.lifecycle.*
import com.example.unchilnote.data.Repository
import com.example.unchilnote.data.dataset.CURRENTLOCATION_TBL
import com.example.unchilnote.data.dataset.CURRENTWEATHER_TBL
import com.example.unchilnote.data.dataset.toCURRENTWEATHER_TBL
import com.example.unchilnote.utils.*
import com.example.unchilnote.write.WriteFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.take

class WeatherViewModel(val repository: Repository): ViewModel() {


    private val logTag = WeatherViewModel::class.java.name
    private val _snackbar = MutableLiveData<String?>()
    val snackbar: LiveData<String?> = _snackbar

    private val _progressBarVisible = MutableLiveData(false)
    val progressBarVisible: LiveData<Boolean> = _progressBarVisible

    lateinit var parentFragmentName : String

    private val beforeLocation
        get() = repository.currentLocation


    val currentWeather: LiveData<CURRENTWEATHER_TBL>
        get() {
            return when( parentFragmentName) {
                DETAILFRAGMENT -> {
                    repository.memoWeatherTbl.map {
                        it.toCURRENTWEATHER_TBL()
                    }.asLiveData()
                }
                else -> {
                    repository.currentWeatherDb.asLiveData()

                }
            }

        }

    fun getCurrentLocation(isGapCheck: Boolean, context: Context) {

        BizLogic.launchDataLoad(viewModelScope, _progressBarVisible, _snackbar) {
            when (isGapCheck) {
                true -> {
                    if ( System.currentTimeMillis() - beforeLocation.dt  > GAP_COLLECT_LOCATION_MIN ) {
                        repository.getDeviceLocation(context)
                        repository.getCurrentWeather()
                    }
                }
                false ->  {
                    repository.getDeviceLocation(context)
                    repository.getCurrentWeather()
                }
            }
        }
    }

    fun getCurrentWeather(isGapCheck: Boolean, context: Context) {
        BizLogic.launchDataLoad(viewModelScope, _progressBarVisible, _snackbar) {
            when (isGapCheck) {
                true -> {
                    if ( System.currentTimeMillis() - beforeLocation.dt  > GAP_COLLECT_LOCATION_MIN ) {
                        repository.getCurrentWeather() }
                }
                false ->  {
                    repository.getCurrentWeather()
                }
            }
        }
    }



    fun clearSnackbarMessage() {
        _snackbar.value = null
    }

}