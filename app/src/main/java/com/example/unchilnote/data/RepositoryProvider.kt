package com.example.unchilnote.data

import android.content.Context
import android.util.Log
import com.example.unchilnote.data.database.DbHandle
import com.example.unchilnote.data.database.LuckDatabase
import com.example.unchilnote.data.network.NetHandle


object RepositoryProvider {
    private var logTag = RepositoryProvider::class.java.name
    private val lock = Any()
    private var database: LuckDatabase? = null
    var repository: Repository? = null

    // DB Truncate 시 사용할 목적으로
    fun init(context: Context) {
        synchronized(lock) {
            database = LuckDatabase.getInstance(context)
        }
    }

    fun getRepository(context: Context) : Repository {

        synchronized(lock) {
            Log.d(logTag, "getRepository repository: [${repository}]")
            return repository ?: createRepository(context)
        }
    }

    private fun createRepository( context: Context): Repository {
        try {
            val newRepository = Repository(DbHandle(context), NetHandle())
            Log.d(logTag, "createRepository: [${newRepository}]")
            repository = newRepository
        }catch (e: Exception) {
            Log.d(logTag, "Fail createRepository:[${e.message}]")
        }
        return repository!!
    }


}