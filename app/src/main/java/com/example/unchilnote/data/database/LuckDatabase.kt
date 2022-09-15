package com.example.unchilnote.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.unchilnote.data.dataset.*
import com.example.unchilnote.utils.DB_NAME


@Database( entities = [ CURRENTLOCATION_TBL::class, CURRENTWEATHER_TBL::class,
                     MEMO_TBL::class, MEMO_TAG_TBL::class, MEMO_FILE_TBL::class,
                        MEMO_WEATHER_TBL::class]
    , version = 1, exportSchema = false)
abstract class LuckDatabase : RoomDatabase() {

    abstract fun luckDao(): LuckDao

    companion object {

        @Volatile
        private var INSTANCE: LuckDatabase? = null

        fun getInstance(context: Context): LuckDatabase {
            synchronized(this) {
                val logTag = LuckDatabase::class.java.name
                Log.d( logTag, "DB Get Instance:[${INSTANCE}]")
                var instance = INSTANCE

                if (instance == null) {

                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        LuckDatabase::class.java,
                        DB_NAME).build()

                    INSTANCE = instance
                    Log.d(logTag, "DB Create Instance:[${INSTANCE}]")
                }
                return instance
            }
        }

    }
}