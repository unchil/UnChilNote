package com.example.unchilnote.record

import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.unchilnote.data.Repository
import com.example.unchilnote.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class RecordViewModel(val repository: Repository) : ViewModel() {
    private val logTag = RecordViewModel::class.java.name

    lateinit var parentFragmentName: String

    var recordList: MutableList<File> =  mutableListOf()


    var _snackbar = MutableLiveData<String?>()
    val snackbar: LiveData<String?> = _snackbar

    var _progressBarVisible = MutableLiveData(false)
    val progressBarVisible: LiveData<Boolean> = _progressBarVisible


    lateinit var recordDirectory: String

    var recordTextMap = repository.recordTextMap



/*
    val msg = ObservableField<String>()

    fun OnTextChanged(s :CharSequence ,start: Int, before: Int, count: Int){
        msg.set(s.toString())
    }
*/

    private fun setRecordFileDirectory(context: Context) {
        recordDirectory =
            FileUtils.getOutputDirectory(context, RECORD, isCache = false).absolutePath
    }



    fun clearSnackbarMessage() {
        _snackbar.value = null
    }

    fun setRecordList(path: String, context: Context) {
        setRecordFileDirectory(context)

        when (parentFragmentName) {
            DETAILFRAGMENT -> {
                repository.memoFileTbl.forEach {
                    when (it.type) {
                        RECORD -> {
                            recordList.add(File(path, it.fileName))
                        }
                    }
                }
            }
            else -> {
                recordList = File(path).listFiles { file ->
                    arrayOf(OGG_CHECK).contains(file.extension.uppercase(Locale.ROOT))
                }?.sortedDescending()?.toMutableList() ?: mutableListOf()
            }

        }
    }

    suspend fun getConvText(fileName: String, context: Context): String {
        return repository.convSpeechToText(fileName, context)
    }



}