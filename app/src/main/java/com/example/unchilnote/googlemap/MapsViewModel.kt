package com.example.unchilnote.googlemap

import androidx.lifecycle.*
import com.example.unchilnote.data.Repository
import com.example.unchilnote.data.dataset.CURRENTLOCATION_TBL
import com.example.unchilnote.data.dataset.MEMO_TBL
import com.example.unchilnote.data.dataset.MarkerItem
import com.example.unchilnote.data.dataset.MemoItem
import com.example.unchilnote.list.ListViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MapsViewModel(val repository: Repository) : ViewModel() {

    private val logTag = ListViewModel::class.java.name

    val markerList: MutableList<MarkerItem> = mutableListOf()
    val memoList: MutableList<MEMO_TBL> = mutableListOf()



    init {
        setMarkerList()
    }

    fun setMarkerList() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markerMemoListDb.take(1).collectLatest {
                it.forEach {
                    memoList.add(it)
                    markerList.add(
                        MarkerItem(
                           latLng = LatLng(it.latitude.toDouble(), it.longitude.toDouble()),
                            myTitle = it.title,
                            mySnippet = it.snippets,
                            writeTime = it.id,
                            desc = it.desc
                        )
                    )
                }
            }
        }
    }

    fun setMemoItem(index:Int) {
        repository.setMemoItem(memoList[index])
    }

    val currentLocation: LiveData<CURRENTLOCATION_TBL>
        get() = repository.currentLocationDb.asLiveData()


}