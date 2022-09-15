package com.example.unchilnote.data.network



import com.example.unchilnote.data.dataset.CURRENTWEATHER_NET
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class OpenWeatherService {
    private val logTag = OpenWeatherService::class.java.name
    private val moshi: Moshi
    lateinit var retrofit: Retrofit
    lateinit var caller: OpenWeather

    init {
        moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }

    fun createOpenWeatherService(BASE_URL: String){
        retrofit = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BASE_URL)
            .build()

        caller = retrofit.create(OpenWeather::class.java)
    }

}

interface OpenWeather {
    @GET("weather")
     suspend fun getWeatherData(@Query("lat")latitude: String,
                               @Query("lon")longitude: String,
                               @Query("units")units: String,
                               @Query("appid")apiKey: String) : CURRENTWEATHER_NET

}
