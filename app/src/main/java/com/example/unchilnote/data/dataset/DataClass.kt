package com.example.unchilnote.data.dataset

import androidx.viewpager2.widget.ViewPager2
import com.example.unchilnote.utils.*
import com.example.unchilnote.utils.ConvFormat.Companion.toSelectedKeyString
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem


data class GalleryCaller (
    var parentFragmentName:String,
    var fileDirectory:String,
    var isSelfCaller: Boolean
)

data class MarkerItem( val latLng: LatLng,
                   val myTitle: String?,
                   val mySnippet: String?,
                   val writeTime:Long,
                    val desc:String?) : ClusterItem {
    override fun getPosition() = latLng
    override fun getTitle() = myTitle
    override fun getSnippet() = mySnippet
}

data class MemoItem (
    var id: Long,
    var recordMap: MutableMap<String, String>,
    var snapshotList: MutableList<String>,
    var photoList: MutableList<String>,
    var tagsMap: MutableMap<String, Boolean>,
    var isSecret: Boolean,
    var isPin: Boolean,
    var currentLocation: CURRENTLOCATION_TBL,
    var currentWeather: CURRENTWEATHER_TBL
)

fun MemoItem.toMEMO_TBL(): MEMO_TBL {
    return MEMO_TBL(
        id = this.id,
        latitude = this.currentLocation.latitude,
        longitude = this.currentLocation.longitude,
        altitude = this.currentLocation.latitude,
        isSecret = this.isSecret,
        isPin = this.isPin,
        title = ConvFormat.UnixTimeToString( id, yyyyMMddHHmmssE) +
                " ${this.currentWeather.main} ${this.currentWeather.name}/${this.currentWeather.country}" ,
        snippets = this.tagsMap.toSelectedKeyString(),
        desc = String.format( ATTACH_CNT_FORMAT,
            this.snapshotList.size, this.recordMap.size, this.photoList.size),
        snapshot = this.snapshotList.first(),
        snapshotCnt = this.snapshotList.size,
        recordCnt = this.recordMap.size,
        photoCnt = this.photoList.size
    )
}

fun MemoItem.toMEMO_TAG_TBL(): MEMO_TAG_TBL {
    return MEMO_TAG_TBL(
        id = this.id,
        climbing = this.tagsMap[CLIMBING]?:false,
        tracking = this.tagsMap[TRACKING]?:false,
        camping =  this.tagsMap[CAMPING]?:false,
        travel =  this.tagsMap[TRAVEL]?:false,
        culture = this.tagsMap[CULTURE]?:false,
        art = this.tagsMap[ART]?:false,
        traffic = this.tagsMap[TRAFFIC]?:false,
        restaurant = this.tagsMap[RESTAURANT]?:false
    )
}

fun MemoItem.toMEMO_FILE_TBL_LIST(): List<MEMO_FILE_TBL> {
    var memoFileTblList = mutableListOf<MEMO_FILE_TBL>()
    this.snapshotList.forEach {
        memoFileTblList.add( MEMO_FILE_TBL(
            id = this.id,
            type = SNAPSHOT,
            fileName = it,
            text = null ))
    }
    this.photoList.forEach {
        memoFileTblList.add( MEMO_FILE_TBL(
            id = this.id,
            type = PHOTO,
            fileName = it,
            text = null ))
    }
    this.recordMap.forEach {
        memoFileTblList.add( MEMO_FILE_TBL(
            id = this.id,
            type = RECORD,
            fileName = it.key,
            text = it.value ))
    }

    return memoFileTblList
}

fun MemoItem.toMEMO_WEATHTER_TBL() : MEMO_WEATHER_TBL {
    this.currentWeather.dt = this.id
    return this.currentWeather.toMEMO_WEATHER_TBL()
}