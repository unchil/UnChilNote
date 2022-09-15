package com.example.unchilnote.data


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.unchilnote.utils.ConvFormat
import com.example.unchilnote.utils.RECORD
import com.google.android.gms.maps.GoogleMap

class SharedViewModel: ViewModel() {
    lateinit var repository: Repository

    var selfCallMap = mutableMapOf<String, Boolean>()

    private val _snackbar = MutableLiveData<String?>()
    val snackbar: LiveData<String?> = _snackbar


    private val _progressBarVisible = MutableLiveData(false)
    val progressBarVisible: LiveData<Boolean> = _progressBarVisible

    var googleMap: GoogleMap? = null
    var googleMapType = GoogleMap.MAP_TYPE_NONE
    var googleMapZoom = 18F

    fun clearSnackbarMessage() {
        _snackbar.value = null
    }


    var playListStat = mutableMapOf<String, Int>()

    
    fun replaceRecordTextMap(key: String, value: String) {
        repository.recordTextMap.replace(key, value)
    }

    fun removeRecordTextMap(key: String) {
        repository.recordTextMap.remove(key)
    }
    fun clearRecordTextMap(){
        repository.recordTextMap.clear()
    }

    fun setRecordTextMap() {

        repository.recordTextMap.clear()
            repository.memoFileTbl.forEach {
                when(it.type) {
                    RECORD -> repository.recordTextMap.put(it.fileName, it.text?: ConvFormat.makeRecordText(it.fileName))
                }
            }


    }


}