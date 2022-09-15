package com.example.unchilnote.data.database

import android.content.Context
import com.example.unchilnote.data.dataset.*
import com.example.unchilnote.utils.*
import com.example.unchilnote.utils.ConvFormat.Companion.toMEMO_TAG_TBL
import com.example.unchilnote.utils.ConvFormat.Companion.toValueString
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class DbHandle (context: Context) {
    private val logTag = DbHandle::class.java.name
    val dao = LuckDatabase.getInstance(context).luckDao()



    suspend fun insertCurrentLocation(it: CURRENTLOCATION_TBL) {
        dao.dCurrentLocation()
        dao.cCurrentLocation(it)
    }

    suspend fun insertCurrentWeather(it: CURRENTWEATHER_TBL) {
        dao.dCurrentWeather()
        dao.cCurrentWeather(it)
    }

    suspend fun deleteMemo(id:Long) {
        dao.dMemo(id)
        dao.dMemoWeather(id)
        dao.dMemoFile(id)
        dao.dMemoTag(id)
    }

    suspend fun insertMemo(it: MemoItem) {
        dao.cMemo(it.toMEMO_TBL())
        dao.cMemoWeather(it.toMEMO_WEATHTER_TBL())
        dao.cMemoTag(it.toMEMO_TAG_TBL())
        dao.cMemoFile(it.toMEMO_FILE_TBL_LIST())
    }

    fun selectMemoWeather(id: Long) : Flow<MEMO_WEATHER_TBL> {
        return dao.rMemoWeather(id).mapNotNull { it }.distinctUntilChanged()
    }

    fun selectrMemoTag(id: Long) : MEMO_TAG_TBL {
        return dao.rMemoTag(id)
    }

    fun selectrMemoFile(id: Long) : List<MEMO_FILE_TBL>{
        return dao.rMemoFile(id)
    }

    fun selectCurrentLocation() : Flow<CURRENTLOCATION_TBL> {
        return dao.rCurrentLocation().mapNotNull { it }.distinctUntilChanged()
    }

    fun selectCurrentWeather() : Flow<CURRENTWEATHER_TBL> {
        return dao.rCurrentWeather().mapNotNull { it }.distinctUntilChanged()
    }

    fun selectMemoList() : Flow<List<MEMO_TBL>> {
        return dao.rMemo().mapNotNull { it }
    }

    fun selectMarkerMemoList() : Flow<List<MEMO_TBL>> {
        return dao.rMarkerMemo().mapNotNull { it }
    }

    fun selectSearchMemoList() :Flow<List<MEMO_TBL>> {
        return dao.rSearchMemo(TAG_INFO.toValueString()).mapNotNull { it }
    }

    suspend fun updateMemoSecret(id: Long, isSecret: Boolean) {
        return dao.uMemoSecret(id, isSecret)
    }

    suspend fun updateMemoPin(id: Long, isPin: Boolean) {
        return dao.uMemoPin(id, isPin)
    }

    suspend fun updateMemoSnippets(id: Long, snippets: String) {
        return dao.uMemoSnippets(id, snippets)
    }

    suspend fun updateMemoTag(memoTagTbl: MEMO_TAG_TBL) {
        dao.cMemoTag(memoTagTbl)
    }

}

