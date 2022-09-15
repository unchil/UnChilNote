package com.example.unchilnote.data.database


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.unchilnote.data.dataset.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LuckDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cCurrentLocation(it: CURRENTLOCATION_TBL)

    @Query("DELETE FROM CURRENTLOCATION_TBL")
    suspend fun dCurrentLocation()

    @Query("SELECT * FROM CURRENTLOCATION_TBL ORDER BY dt DESC LIMIT 1 ")
    fun rCurrentLocation(): Flow<CURRENTLOCATION_TBL>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cCurrentWeather(it: CURRENTWEATHER_TBL)

    @Query("DELETE FROM CURRENTWEATHER_TBL")
    suspend fun dCurrentWeather()

    @Query("SELECT * FROM CURRENTWEATHER_TBL ORDER BY dt DESC LIMIT 1 ")
    fun rCurrentWeather(): Flow<CURRENTWEATHER_TBL>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cMemo(it : MEMO_TBL)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cMemoWeather(it : MEMO_WEATHER_TBL)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cMemoTag(it : MEMO_TAG_TBL)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cMemoFile(it : List<MEMO_FILE_TBL>)

    @Query("SELECT * FROM MEMO_TBL ORDER BY id DESC")
    fun rMemo(): Flow<List<MEMO_TBL>>

    @Query("SELECT * FROM MEMO_TBL WHERE isPin = 1  ORDER BY id DESC")
    fun rMarkerMemo(): Flow<List<MEMO_TBL>>

    @Query("SELECT * FROM MEMO_TBL WHERE id in " +
                                    "( SELECT T2.id  FROM ( SELECT id, " +
                                    "   printf(  '%d%d%d%d%d%d%d%d' , " +
                                    "    CASE WHEN climbing THEN '1' ELSE '0' END , " +
                                    "    CASE WHEN tracking THEN '1' ELSE '0' END , " +
                                    "    CASE WHEN camping THEN '1' ELSE '0' END , " +
                                    "    CASE WHEN travel THEN '1' ELSE '0' END , " +
                                    "    CASE WHEN culture THEN '1' ELSE '0' END , " +
                                    "    CASE WHEN art THEN '1' ELSE '0' END , " +
                                    "    CASE WHEN traffic THEN '1' ELSE '0' END , " +
                                    "    CASE WHEN restaurant THEN '1' ELSE '0' END " +
                                    "   ) as tagString " +
                                     "FROM MEMO_TAG_TBL " +
                                     "WHERE " +
                                        "substr(tagString, 1,1) = '1' AND SUBSTR( :tags, 1, 1) = '1' " +
                                        "or substr(tagString, 2,1) = '1' AND SUBSTR( :tags, 2, 1) = '1' " +
                                        "or substr(tagString, 3,1) = '1' AND SUBSTR( :tags, 3, 1) = '1' " +
                                        "or substr(tagString, 4,1) = '1' AND SUBSTR( :tags, 4, 1) = '1' " +
                                        "or substr(tagString, 5,1) = '1' AND SUBSTR( :tags, 5, 1) = '1' " +
                                        "or substr(tagString, 6,1) = '1' AND SUBSTR( :tags, 6, 1) = '1' " +
                                        "or substr(tagString, 7,1) = '1' AND SUBSTR( :tags, 7, 1) = '1' " +
                                        "or substr(tagString, 8,1) = '1' AND SUBSTR( :tags, 8, 1) = '1' " +
                                    ")  T1, MEMO_TAG_TBL T2 " +
                                         "WHERE T2.id = T1.id " +
            " ) ORDER BY id DESC " )
    fun rSearchMemo(tags: String): Flow<List<MEMO_TBL>>


    @Query("SELECT * FROM MEMO_WEATHER_TBL  WHERE id = :id LIMIT 1")
    fun rMemoWeather(id: Long): Flow<MEMO_WEATHER_TBL>

    @Query("SELECT * FROM MEMO_TAG_TBL WHERE id = :id LIMIT 1")
    fun rMemoTag(id: Long): MEMO_TAG_TBL

    @Query("SELECT * FROM MEMO_FILE_TBL WHERE id = :id ")
    fun rMemoFile(id:Long): List<MEMO_FILE_TBL>


    @Query("UPDATE MEMO_TBL SET isSecret =:isSecret WHERE id =:id")
    suspend fun uMemoSecret(id:Long, isSecret:Boolean)

    @Query("UPDATE MEMO_TBL SET isPin =:isPin WHERE id =:id")
    suspend fun uMemoPin(id:Long, isPin:Boolean)


    @Query("UPDATE MEMO_TBL SET snippets =:snippets WHERE id =:id")
    suspend fun uMemoSnippets(id:Long, snippets:String)


    @Query("DELETE FROM MEMO_TBL WHERE id = :id ")
    suspend fun dMemo(id:Long)

    @Query("DELETE FROM MEMO_WEATHER_TBL WHERE id = :id ")
    suspend fun dMemoWeather(id:Long)

    @Query("DELETE FROM MEMO_TAG_TBL WHERE id = :id ")
    suspend fun dMemoTag(id:Long)

    @Query("DELETE FROM MEMO_FILE_TBL WHERE id = :id ")
    suspend fun dMemoFile(id:Long)



}