package com.example.unchilnote.detail

import android.content.Context
import android.os.Bundle
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.unchilnote.R
import com.example.unchilnote.data.Repository
import com.example.unchilnote.utils.*
import com.example.unchilnote.utils.ConvFormat.Companion.toMEMO_TAG_TBL
import com.example.unchilnote.weather.WeatherViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import java.io.File

class DetailViewModel(val repository: Repository) : ViewModel() {
    private val logTag = WeatherViewModel::class.java.name

    val _snackbar = MutableLiveData<String?>()
    val snackbar: LiveData<String?> = _snackbar

    private val _progressBarVisible = MutableLiveData(false)
    val progressBarVisible: LiveData<Boolean> = _progressBarVisible

    var isChipsDisplay = false
    var isSecret: Boolean = false
    var isPin: Boolean = false
    var isUpdate: Boolean = false
    var memoId = 0L
    val memoTbl = repository.memoTbl.filter { it.id != 0L }.take(1).asLiveData()
    val memoTagTbl = repository.memoTagTbl


    private lateinit var snapshotFolder: File
    private lateinit var photoFolder: File
    lateinit var recordFolder: File

    var args_Snapshot = Bundle()
    var args_Photo = Bundle()
    var args_record = Bundle()

    fun updateMemo(id: Int?) {
        when(id) {
            R.id.menu_detail_label -> {
                repository.updateMemoLabel(memoId)
            }
            R.id.menu_detail_secret -> {
                repository.updateMemoSecret(memoId,  isSecret)
            }
            R.id.menu_detail_pin -> {
                repository.updateMemoPin(memoId, isPin)
            }
            null -> {
                repository.updateMemoSecret(memoId,  isSecret)
                repository.updateMemoLabel(memoId)
                repository.updateMemoPin(memoId, isPin)
            }
        }
    }

    fun makeArgs(context: Context){
        snapshotFolder = FileUtils.getOutputDirectory(context, SNAPSHOT, isCache = false)
        photoFolder = FileUtils.getOutputDirectory(context, PHOTO, isCache = false)
        recordFolder = FileUtils.getOutputDirectory(context, RECORD, isCache = false)
        args_Snapshot.putString(ARG_ROOTDIR, snapshotFolder.absolutePath)
        args_Photo.putString(ARG_ROOTDIR, photoFolder.absolutePath)
        args_record.putString(ARG_ROOTDIR, recordFolder.absolutePath)
    }




    fun clearSnackbarMessage() {
        _snackbar.value = null
    }

}