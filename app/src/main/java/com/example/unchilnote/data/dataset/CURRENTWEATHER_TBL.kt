package com.example.unchilnote.data.dataset

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.unchilnote.utils.*

@Entity(tableName = "CURRENTWEATHER_TBL")
data class CURRENTWEATHER_TBL(
    @PrimaryKey(autoGenerate = false)
    @NonNull
//        var writeTime: Long,
    var dt: Long,
    var base: String ,
    var visibility: Int,
    var timezone: Long,
    var name: String,
    var latitude: Float,
    var longitude: Float,
    //       var altitude: Float,
    var main: String,
    var description: String,
    var icon : String,
    var temp: Float,
    var feels_like: Float,
    var pressure: Float,
    var humidity: Float,
    var temp_min: Float,
    var temp_max: Float,
    var speed : Float,
    var deg : Float,
    var all  : Int,
    var type : Int,
    var country : String,
    var sunrise : Long,
    var sunset : Long

)


fun CURRENTWEATHER_TBL.toTextHeadLine(): String {
    return ConvFormat.UnixTimeToString(this.dt, yyyyMMddHHmmE) + " ${this.main} ${this.name}/${this.country}"
}

fun CURRENTWEATHER_TBL.toTextSun(): String {
    return String.format( WEATHER_TEXT_SUN,
        ConvFormat.UnixTimeToString(this.sunrise, EEEHHmmss),
        ConvFormat.UnixTimeToString(this.sunset, EEEHHmmss)
    )
}

fun CURRENTWEATHER_TBL.toTextTemp(): String {
    return String.format ( WEATHER_TEXT_TEMP,
        this.temp,
        this.temp_min,
        this.temp_max
    )
}


fun CURRENTWEATHER_TBL.toTextWeather(): String {
    return String.format( WEATHER_TEXT_WEATHER,
        this.pressure,
        this.humidity
    ) + "%"
}


fun CURRENTWEATHER_TBL.toTextWind(): String {
    return   String.format(
        WEATHER_TEXT_WIND,
        this.speed,
        this.deg,
        this.visibility/ TAG_M_KM )
}

fun CURRENTWEATHER_TBL.toMEMO_WEATHER_TBL(): MEMO_WEATHER_TBL {
    return MEMO_WEATHER_TBL(
        id = this.dt,
        base = this.base,
        visibility = this.visibility,
        timezone = this.timezone,
        name = this.name,
        latitude = this.latitude,
        longitude = this.longitude,
        main = this.main,
        description = this.description,
        icon = this.icon,
        temp = this.temp,
        feels_like = this.feels_like,
        pressure = this.pressure,
        humidity = this.humidity,
        temp_min = this.temp_min,
        temp_max = this.temp_max,
        speed = this.speed,
        deg = this.deg,
        all = this.all,
        type = this.type,
        country = this.country,
        sunrise = this.sunrise,
        sunset =this.sunset
    )
}