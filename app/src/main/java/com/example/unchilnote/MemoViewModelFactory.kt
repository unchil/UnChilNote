package com.example.unchilnote

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unchilnote.data.RepositoryProvider
import com.example.unchilnote.detail.DetailViewModel
import com.example.unchilnote.gallery.GalleryViewModel
import com.example.unchilnote.googlemap.MapsViewModel
import com.example.unchilnote.list.ListViewModel
import com.example.unchilnote.record.RecordViewModel
import com.example.unchilnote.weather.WeatherViewModel
import com.example.unchilnote.write.WriteViewModel

class MemoViewModelFactory (context: Context) : ViewModelProvider.Factory {

    val repository = RepositoryProvider.getRepository(context)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        return when(modelClass.name){
            ListViewModel::class.java.name -> ListViewModel(repository) as T
            WriteViewModel::class.java.name -> WriteViewModel(repository) as T
            DetailViewModel::class.java.name -> DetailViewModel(repository) as T
            WeatherViewModel::class.java.name -> WeatherViewModel(repository) as T
            RecordViewModel::class.java.name -> RecordViewModel(repository) as T
            GalleryViewModel::class.java.name -> GalleryViewModel(repository) as T
            MapsViewModel::class.java.name -> MapsViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }

    }


}
