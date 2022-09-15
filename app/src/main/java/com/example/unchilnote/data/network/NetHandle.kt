package com.example.unchilnote.data.network

import android.content.Context
import com.example.unchilnote.data.dataset.CURRENTWEATHER_NET
import com.example.unchilnote.utils.OPENWEATHER_KEY
import com.example.unchilnote.utils.OPENWEATHER_UNITS
import com.example.unchilnote.utils.OPENWEATHER_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class NetHandle {
    private val logTag = NetHandle::class.java.name
    private val openWeatherService: OpenWeatherService = OpenWeatherService()

    init {
        openWeatherService.createOpenWeatherService(OPENWEATHER_URL)
    }

     suspend fun recvCurrentWeather( lat :String,  lon:String ): CURRENTWEATHER_NET {
            return openWeatherService.caller.getWeatherData(lat, lon, OPENWEATHER_UNITS, OPENWEATHER_KEY)
    }


     suspend fun callSpeechToText(fileName:String, context: Context): String {
        val convText =  withContext(Dispatchers.IO) {
            GoogleSpeechToTextService(context.resources).asyncRecognizeFile(fileName)
        }
         return convText
    }

}