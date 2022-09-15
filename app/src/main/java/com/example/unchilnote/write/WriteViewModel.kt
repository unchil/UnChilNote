package com.example.unchilnote.write

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unchilnote.data.Repository
import com.example.unchilnote.data.dataset.MemoItem
import com.example.unchilnote.utils.*
import com.example.unchilnote.utils.BizLogic.Companion.setTagsMap
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.util.*
import kotlin.collections.set


class WriteViewModel (val repository: Repository): ViewModel() {
    private val logTag = WriteViewModel::class.java.name

    var googleMap: GoogleMap? = null

    var _snackbar = MutableLiveData<String?>()
    val snackbar: LiveData<String?> = _snackbar

    var _progressBarVisible = MutableLiveData(false)
    val progressBarVisible: LiveData<Boolean> = _progressBarVisible

    var isDrawingOnMap = false
    var isOnRecording = false
    var isChipsDisplay = false
    var _latLngList: MutableList<LatLng> = mutableListOf()
    var polyLineOptions : MutableList<PolylineOptions> = mutableListOf()

    lateinit var recorder: MediaRecorder

    private lateinit var defaultMapFolder: File
    private lateinit var cacheMapFolder: File
    private lateinit var cachePhotoFolder: File
    lateinit var cacheRecordFolder: File

    var galleryFragmentArgs_Map = Bundle()
    var galleryFragmentArgs_Photo = Bundle()
    var recordFragmentArgs_record = Bundle()

    private var _photoList = mutableListOf<String>()
    private var _snapshotList = mutableListOf<String>()
    var isSecret: Boolean = false
    var isPin: Boolean = false


    fun putRecordTextMap(key: String, value: String) {
        repository.recordTextMap.put(key, value)
    }

    fun clearRecordTextMap(){
        repository.recordTextMap.clear()
    }

    fun saveMemo(context: Context, chipGroup: ChipGroup) {

        setPhotoList()
        if ( setSnapShotList(cacheMapFolder) == 0) {
            setSnapShotList(defaultMapFolder)
            FileUtils.moveAttachFile(context, DIR_DEFAULT)
        } else {
            FileUtils.moveAttachFile(context, SNAPSHOT)
        }
        viewModelScope.launch(Dispatchers.IO) {
            //--
            //-- (For Setting recordMap) waiting time for RecordViewerFragment::onDestroyView to finish
            delay(DELAYTIME_SETTING_RECORD_MAP)
            //--
            //--
            repository.saveMemoItem(
                MemoItem(
                    id = Instant.now().epochSecond,
                    recordMap = repository.recordTextMap,
                    snapshotList = _snapshotList,
                    photoList = _photoList,
                    tagsMap = setTagsMap(chipGroup),
                    isSecret = isSecret,
                    isPin = isPin,
                    currentLocation = repository.currentLocation,
                    currentWeather = repository.currentWeather
                )
            )
        }
    }

    fun makeSnapShot(type: String, fragment: WriteFragment?) {
        googleMap?.let { googleMap ->
            viewModelScope.launch {
                googleMap.snapshot {
                    it?.let {
                        when(type){
                            SNAPSHOT_DEFAULT -> {
                                saveSnapShotMap(it, type)
                            }
                            SNAPSHOT_DRAWING -> {
                                saveSnapShotMap(it, type)
                                googleMap.clear()
                                polyLineOptions.clear()
                                fragment?.replaceContainer(SNAPSHOT)
                            }
                        }
                    }
                }
            }
        }

    }

    private fun saveSnapShotMap(bitmap: Bitmap, type: String) {
        val folder = when(type){
            SNAPSHOT_DEFAULT -> defaultMapFolder
            SNAPSHOT_DRAWING -> cacheMapFolder
            else -> return
        }
        val drawingMapFile =
            FileUtils.createFile(folder, FILENAME_FORMAT, EXTENSION_MAP)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(drawingMapFile))
    }

    private fun setPhotoList() {
        cachePhotoFolder.listFiles { file ->
            arrayOf(JPG_CHECK).contains(file.extension.uppercase(Locale.ROOT))
        }?.forEach {
            _photoList.add(it.name)
        }
    }

    private fun setSnapShotList(folder:File): Int {
        folder.listFiles { file ->
            arrayOf(JPG_CHECK).contains(file.extension.uppercase(Locale.ROOT))
        }?.forEach {
            _snapshotList.add(it.name)
        }
        return _snapshotList.size
    }

    fun setGalleryFragmentArgs(context: Context){
        defaultMapFolder = FileUtils.getOutputDirectory(context, DIR_DEFAULT, isCache = true)
        cacheMapFolder = FileUtils.getOutputDirectory(context, SNAPSHOT, isCache = true)
        cachePhotoFolder = FileUtils.getOutputDirectory(context, PHOTO, isCache = true)
        cacheRecordFolder = FileUtils.getOutputDirectory(context, RECORD, isCache = true)
        galleryFragmentArgs_Map.putString(ARG_ROOTDIR, cacheMapFolder.absolutePath)
        galleryFragmentArgs_Photo.putString(ARG_ROOTDIR, cachePhotoFolder.absolutePath)
        recordFragmentArgs_record.putString(ARG_ROOTDIR, cacheRecordFolder.absolutePath)
    }

    fun clearSnackbarMessage() {
        _snackbar.value = null
    }


}