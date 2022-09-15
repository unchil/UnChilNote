package com.example.unchilnote.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.example.unchilnote.data.database.DbHandle
import com.example.unchilnote.data.dataset.*
import com.example.unchilnote.data.network.NetHandle
import com.example.unchilnote.utils.*
import com.example.unchilnote.utils.ConvFormat.Companion.toCurrentLocation_TBL
import com.example.unchilnote.utils.ConvFormat.Companion.toMEMO_TAG_TBL
import com.example.unchilnote.utils.ConvFormat.Companion.toSelectedKeyString
import com.example.unchilnote.utils.ConvFormat.Companion.toValueString
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class Repository ( val dbHandle: DbHandle, val netHandle: NetHandle) {

    private val logTag = Repository::class.java.name

    val message = "What a Wonderful World !!!"

    var currentLocation = INIT_CURRENT_LOCATION
    var currentWeather = INIT_CURRENT_WEATHER

    var _fCurrentLocation: MutableStateFlow<CURRENTLOCATION_TBL>
        = MutableStateFlow(INIT_CURRENT_LOCATION)

    val currentLocationDb: Flow<CURRENTLOCATION_TBL>
         get() = dbHandle.selectCurrentLocation()

    val currentWeatherDb: Flow<CURRENTWEATHER_TBL>
        get() = dbHandle.selectCurrentWeather()

    val memoListDb: Flow<List<MEMO_TBL>>
        get() = dbHandle.selectMemoList()

    val markerMemoListDb: Flow<List<MEMO_TBL>>
        get() = dbHandle.selectMarkerMemoList()


    val searchMemoListDb: Flow<List<MEMO_TBL>>
        get() = dbHandle.selectSearchMemoList()

    var recordTextMap = mutableMapOf<String, String>()


    val memoTbl: MutableStateFlow<MEMO_TBL>
        = MutableStateFlow(INIT_CURRENT_MEMO)

    lateinit var memoWeatherTbl: Flow<MEMO_WEATHER_TBL>
    lateinit var  memoFileTbl: List<MEMO_FILE_TBL>
    lateinit var  memoTagTbl: MEMO_TAG_TBL


    fun updateMemoLabel(id: Long)
        = CoroutineScope(Dispatchers.IO).launch {
            dbHandle.updateMemoSnippets(id, TAG_INFO.toSelectedKeyString())
            dbHandle.updateMemoTag(TAG_INFO.toMEMO_TAG_TBL(id))
        }

    fun updateMemoSecret(id: Long, isSecret: Boolean)
        = CoroutineScope(Dispatchers.IO).launch {
            dbHandle.updateMemoSecret(id, isSecret)
        }

    fun updateMemoPin(id: Long, isPin : Boolean)
         = CoroutineScope(Dispatchers.IO).launch {
            dbHandle.updateMemoPin(id, isPin)
        }

    fun setMemoItem(memo: MEMO_TBL)  {
        CoroutineScope(Dispatchers.IO).launch {
            memoTbl.emit(memo)
            memoTbl.replayCache

            memoFileTbl = dbHandle.selectrMemoFile(memo.id)
            memoTagTbl = dbHandle.selectrMemoTag(memo.id)
        }

        memoWeatherTbl = dbHandle.selectMemoWeather(memo.id)
    }

    suspend fun deleteMemoItem( id : Long) {
        dbHandle.deleteMemo(id)
    }


    suspend fun saveMemoItem (item : MemoItem) {
        dbHandle.insertMemo(item)
    }

    @SuppressLint("MissingPermission")
    suspend fun getDeviceLocation(context: Context)  {
        LocationServices.getFusedLocationProviderClient(context)
            .requestLocationUpdates( LocationRequest.create(),
                object: LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        currentLocation = locationResult.lastLocation.toCurrentLocation_TBL()

                        CoroutineScope(Dispatchers.IO).launch {
                            dbHandle.insertCurrentLocation(currentLocation)
                        }

                        CoroutineScope(Dispatchers.Unconfined).launch {
                            _fCurrentLocation.apply {
                                emit(currentLocation)
                                _fCurrentLocation.replayCache
                           }
                        }
                    }
                },
                Looper.getMainLooper())
    } // getDeviceLocation

     suspend fun getCurrentWeather() {
         _fCurrentLocation.filter { it.dt != 0L }.take(1).collectLatest{
             if ( System.currentTimeMillis() - currentWeather.dt * MILLISEC_CONV_DIGIT > GAP_COLLECT_WEATHER_MIN ) {
                 CoroutineScope(Dispatchers.IO).launch {
                    val result = netHandle.recvCurrentWeather( it.latitude.toString(), it.longitude.toString() )
                    if (result.cod == OPENWEATHER_RESULT_OK && result.dt > currentWeather.dt) {
                        currentWeather = result.toCURRENTWEATHER_TBL()
                        dbHandle.insertCurrentWeather(currentWeather)
                    }
                 }
             }
         }
    }

    suspend fun convSpeechToText(fileName: String, context: Context) :String {
        return netHandle.callSpeechToText( fileName, context)
    }

}