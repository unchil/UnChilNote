package com.example.unchilnote.data.dataset

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.unchilnote.utils.*


@Entity(tableName = "MEMO_TBL")
data class MEMO_TBL(
    @PrimaryKey(autoGenerate = false)
    @NonNull
    var id: Long,
    var latitude: Float,
    var longitude: Float,
    var altitude: Float,
    var isSecret: Boolean,
    var isPin: Boolean,
    var title: String,
    var snippets: String,
    var desc: String,
    var snapshot: String,
    var snapshotCnt: Int,
    var recordCnt: Int,
    var photoCnt: Int
)



@Entity(tableName = "MEMO_TAG_TBL")
data class MEMO_TAG_TBL(
    @PrimaryKey(autoGenerate = false)
    @NonNull
    var id: Long,
    var climbing: Boolean,
    var tracking: Boolean,
    var camping: Boolean,
    var travel: Boolean,
    var culture: Boolean,
    var art: Boolean,
    var traffic: Boolean,
    var restaurant: Boolean
)


@Entity(tableName = "MEMO_FILE_TBL", primaryKeys = arrayOf("id", "type", "fileName"))
data class MEMO_FILE_TBL(
    var id: Long,
    var type: String,
    var fileName: String,
    var text: String?
)


@Entity(tableName = "MEMO_WEATHER_TBL")
data class MEMO_WEATHER_TBL(
    @PrimaryKey(autoGenerate = false)
    @NonNull
    var id: Long,
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


fun MEMO_WEATHER_TBL.toTextHeadLine(): String {
    return ConvFormat.UnixTimeToString(this.id, yyyyMMddHHmmE) + " ${this.main} ${this.name}/${this.country}"
}

fun MEMO_WEATHER_TBL.toTextSun(): String {
    return String.format( WEATHER_TEXT_SUN,
        ConvFormat.UnixTimeToString(this.sunrise, EEEHHmmss),
        ConvFormat.UnixTimeToString(this.sunset, EEEHHmmss)
    )
}

fun MEMO_WEATHER_TBL.toTextTemp(): String {
    return String.format ( WEATHER_TEXT_TEMP,
        this.temp,
        this.temp_min,
        this.temp_max
    )
}


fun MEMO_WEATHER_TBL.toTextWeather(): String {
    return String.format( WEATHER_TEXT_WEATHER,
        this.pressure,
        this.humidity
    ) + "%"
}


fun MEMO_WEATHER_TBL.toTextWind(): String {
    return   String.format(
        WEATHER_TEXT_WIND,
        this.speed,
        this.deg,
        this.visibility/ TAG_M_KM )
}


fun MEMO_WEATHER_TBL.toCURRENTWEATHER_TBL(): CURRENTWEATHER_TBL {
    return CURRENTWEATHER_TBL(
        dt = this.id,
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