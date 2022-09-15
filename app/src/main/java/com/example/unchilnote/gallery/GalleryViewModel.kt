package com.example.unchilnote.gallery



import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.unchilnote.data.Repository
import com.example.unchilnote.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


class GalleryViewModel(val repository: Repository): ViewModel() {
    private val logTag = GalleryViewModel::class.java.name

    var _isGallerySelfCall: Boolean = false
    
    var imageList: MutableList<File> = mutableListOf()

     lateinit var snapshotDirectory: String
     lateinit var photoDirectory: String


    private fun setGalleryFileDirectory(context: Context) {
        snapshotDirectory =
            FileUtils.getOutputDirectory(context, SNAPSHOT, isCache = false).absolutePath
        photoDirectory =
            FileUtils.getOutputDirectory(context, PHOTO, isCache = false).absolutePath
    }


    fun setImageList(fileDirecotry: String, context: Context) {

        setGalleryFileDirectory(context)

        when (fileDirecotry) {
            snapshotDirectory -> {
                repository.memoFileTbl.forEach {
                    if (it.type == SNAPSHOT) imageList.add(File(fileDirecotry, it.fileName))
                }
            }
            photoDirectory -> {
                repository.memoFileTbl.forEach {
                    if (it.type == PHOTO) imageList.add(File(fileDirecotry, it.fileName))
                }
            }
            else -> {
                imageList =
                    File(fileDirecotry).listFiles { file ->
                        arrayOf(JPG_CHECK).contains(file.extension.uppercase(Locale.ROOT))
                    }?.sortedDescending()?.toMutableList() ?: mutableListOf()

            }

        }
    }



}