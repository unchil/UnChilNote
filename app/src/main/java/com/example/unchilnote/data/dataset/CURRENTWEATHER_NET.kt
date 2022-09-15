package com.example.unchilnote.data.dataset

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class CURRENTWEATHER_NET(
    var coord: Coord,
    var weather: List<Weather>,
    var base: String, //Internal parameter
    var main: Main,
    var visibility: Int, // visibility
    var wind: Wind,
    var clouds: Clouds,
    var dt: Long, //Time of data calculation, unix, UTC
    var sys: Sys,
    var timezone: Long, //Shift in seconds from UTC
    var id: Long, //City ID
    var name: String, //City name
    var cod: Int // Return Result Code
    //   var rain: Rain,
    //   var snow: Snow,

)


@JsonClass(generateAdapter = true)
data class Coord (
    var lon: Float, //City geo location, longitude
    var lat: Float //City geo location, latitude
)

@JsonClass(generateAdapter = true)
data class Weather ( //more info Weather condition codes
    var id: Int, //Weather condition id
    var main: String, //Group of weather parameters (Rain, Snow, Extreme etc.)
    var description: String, //Weather condition within the group. You can get the
    var icon : String //Weather icon id
)

@JsonClass(generateAdapter = true)
data class Main (
    var temp: Float, //Temperature. Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
    var feels_like: Float, //Temperature. This temperature parameter accounts for the human perception of weather. Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
    var pressure: Float, //Atmospheric pressure (on the sea level, if there is no sea_level or grnd_level data), hPa
    var humidity: Float, //Humidity, %
    var temp_min: Float, //Minimum temperature at the moment. This is minimal currently observed temperature (within large megalopolises and urban areas). Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
    var temp_max: Float //Maximum temperature at the moment. This is maximal currently observed temperature (within large megalopolises and urban areas). Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
  //  var sea_level: String, //Atmospheric pressure on the sea level, hPa
 //    var grnd_level: String //Atmospheric pressure on the ground level, hPa
)

@JsonClass(generateAdapter = true)
data class Wind (
    var speed : Float, //Wind speed. Unit Default: meter/sec, Metric: meter/sec, Imperial: miles/hour.
    var deg : Float //Wind direction, degrees (meteorological)
  //  var gust : String //Wind gust. Unit Default: meter/sec, Metric: meter/sec, Imperial:miles/hour
)

@JsonClass(generateAdapter = true)
data class Clouds (
    var all  : Int //Cloudiness, %
)

@JsonClass(generateAdapter = true)
data class Rain (
    @Json(name="1h")var oneH : String, //Rain volume for the last 1 hour, mm
    @Json(name="3h")var threeH : String //Rain volume for the last 3 hours, mm
)

@JsonClass(generateAdapter = true)
data class Snow (
    @Json(name="1h")var oneH : String, //Snow volume for the last 1 hour, mm
    @Json(name="3h")var threeH : String //Snow volume for the last 3 hours, mm
)

@JsonClass(generateAdapter = true)
data class Sys (
 //   var type : Int, //Internal parameter
//    var id : Int, //Internal parameter
    var country : String, //Country code (GB, JP etc.)
    var sunrise : Long, //Sunrise time, unix, UTC
    var sunset : Long //Sunset time, unix, UTC
    //    var message : Float, //Internal parameter
)



fun CURRENTWEATHER_NET.toCURRENTWEATHER_TBL(): CURRENTWEATHER_TBL {
    return CURRENTWEATHER_TBL(
        dt = this.dt,
        base = this.base,
        visibility = this.visibility,
        timezone = this.timezone,
        name = this.name,
        latitude = this.coord.lat,
        longitude = this.coord.lon,
        main = this.weather[0].main,
        description = this.weather[0].description,
        icon = this.weather[0].icon,
        temp = this.main.temp,
        feels_like = this.main.feels_like,
        pressure = this.main.pressure,
        humidity = this.main.humidity,
        temp_min = this.main.temp_min,
        temp_max = this.main.temp_max,
        speed = this.wind.speed,
        deg = this.wind.deg,
        all = this.clouds.all,
      //  type = this.sys.type,
        type = 0,
        country = this.sys.country,
        sunrise = this.sys.sunrise,
        sunset = this.sys.sunset
    )
}