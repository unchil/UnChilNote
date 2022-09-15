package com.example.unchilnote.list


import android.content.Context
import android.media.MediaScannerConnection
import androidx.lifecycle.*
import com.example.unchilnote.data.Repository
import com.example.unchilnote.data.dataset.MEMO_FILE_TBL
import com.example.unchilnote.data.dataset.MEMO_TBL
import com.example.unchilnote.utils.*
import kotlinx.coroutines.launch
import java.io.File


class ListViewModel( val repository: Repository): ViewModel() {

    private val logTag = ListViewModel::class.java.name

    val _snackbar = MutableLiveData<String?>()
    val snackbar: LiveData<String?> = _snackbar

    private val _progressBarVisible = MutableLiveData(false)
    val progressBarVisible: LiveData<Boolean> = _progressBarVisible

    var isChipsDisplay = false

    var biometricCheckType = BIOMETRIC_CHECK_TYPE_VIEW

    fun getMemoFileTbl():  List<MEMO_FILE_TBL> {
        return repository.memoFileTbl
    }

    fun deleteMemo(memo: MEMO_TBL, context: Context) {

        getMemoFileTbl().forEach {
           when(it.type) {
                SNAPSHOT -> File( FileUtils.getOutputDirectory(context, SNAPSHOT, false), it.fileName ).delete()
                PHOTO -> File( FileUtils.getOutputDirectory(context, PHOTO, false), it.fileName ).delete()
                RECORD -> File( FileUtils.getOutputDirectory(context, RECORD, false), it.fileName ).delete()
            }
        }

        viewModelScope.launch {
            repository.deleteMemoItem(memo.id)
        }
    }

    val searchMemoList : LiveData<List<MEMO_TBL>>
        get() = repository.searchMemoListDb.asLiveData()

    val memoList: LiveData<List<MEMO_TBL>>
        get() = repository.memoListDb.asLiveData()


    fun setMemoItem(memo:MEMO_TBL) {
        repository.setMemoItem(memo)
    }

    fun clearSnackbarMessage() {
        _snackbar.value = null
    }



}