package com.example.unchilnote.utils

import com.example.unchilnote.data.dataset.CURRENTLOCATION_TBL
import com.example.unchilnote.data.dataset.CURRENTWEATHER_TBL
import com.example.unchilnote.data.dataset.MEMO_TBL
import com.example.unchilnote.list.ListFragment
import com.example.unchilnote.write.WriteFragment
import com.google.android.material.snackbar.Snackbar


typealias LumaListener = (luma: Double) -> Unit

const val LISTFRAGMENT = "com.example.unchilnote.list.ListFragment"
const val DETAILFRAGMENT = "com.example.unchilnote.detail.DetailFragment"
const val WRITEFRAGMENT = "com.example.unchilnote.write.WriteFragment"
const val CAMERAFRAGMENT = "com.example.unchilnote.camera.CameraFragment"


const val WRITE_MENU_LABEL_IDX = 0
const val WRITE_MENU_SECRET_IDX = 1
const val WRITE_MENU_PIN_IDX = 2
const val WRITE_MENU_SNAPSHOT_IDX = 5
const val WRITE_MENU_RECORD_IDX = 6

const val LIST_MENU_LABEL_IDX = 1


const val DETAIL_MENU_LABEL_IDX = 0
const val DETAIL_MENU_SECRET_IDX = 1
const val DETAIL_MENU_PIN_IDX = 2



const val SWIPEHOLDINGWIDTH = 280f

const val RECORD = "RECORD"
const val SNAPSHOT = "SNAPSHOT"
const val PHOTO = "PHOTO"


const val DIR_DEFAULT = "default"



const val CLIMBING = "Climbing"
const val TRACKING = "Tracking"
const val CAMPING = "Camping"
const val TRAVEL = "Travel"
const val CULTURE = "Culture"
const val ART = "Art"
const val TRAFFIC = "Traffic"
const val RESTAURANT = "Restaurant"


const val CHECK = "Check"

const val Snackbar_LENGTH = 600

val TAG_INFO  = mutableMapOf<String, Boolean>().apply {
    put(CLIMBING , false)
    put(TRACKING , false)
    put(CAMPING , false)
    put(TRAVEL , false)
    put(CULTURE , false)
    put(ART , false)
    put(TRAFFIC , false)
    put(RESTAURANT , false)
}

val INIT_CURRENT_LOCATION = CURRENTLOCATION_TBL(0L,0F,0F,0F)

var INIT_CURRENT_MEMO = MEMO_TBL (
    id = 0L,
    latitude = 0f,
    longitude = 0f,
    altitude = 0f,
    isSecret = false,
    isPin = false,
    title = "",
    snippets = "",
    desc = "",
    snapshot = "",
    snapshotCnt = 0,
    recordCnt = 0,
    photoCnt = 0
)

val INIT_CURRENT_WEATHER = CURRENTWEATHER_TBL(
    dt = 0L,
    base = "",
    visibility = 0,
    timezone = 0L,
    name = "",
    latitude = 0F,
    longitude = 0F,
    main = "",
    description = "",
    icon = "",
    temp = 0F,
    feels_like = 0F,
    pressure = 0F,
    humidity = 0F,
    temp_min = 0F,
    temp_max = 0F,
    speed = 0F,
    deg = 0F,
    all = 0,
    type = 0,
    country = "",
    sunrise = 0L,
    sunset = 0L)



const val BIOMETRIC_CHECK_TYPE_VIEW = "VIEW"
const val BIOMETRIC_CHECK_TYPE_SHARE = "SHARE"
const val BIOMETRIC_CHECK_TYPE_DELETE = "DELETE"

const val DELAYTIME_SETTING_RECORD_MAP = 500L  // timeMillis: Long

enum class NetServiceStatus { LOADING, ERROR, DONE }
// OggOpus sampleRateHertz must be one of 8000, 12000, 16000, 24000, or 48000
const val SPEECHTOTEXT_SAMPLERATE = 8000
const val SPEECHTOTEXT_LANGUAGE = "ko-KR"

const val DB_NAME = "LuckDatabase"
const val VELOCITY_CURRENT = 500
const val VELOCITY_ENABLE_SWIPE_MIN = 1500
const val SCALE_MAX = 0.5f // 2배
const val SCALE_MIN = 5.0f // 0.5배

const val FILEPROVIDER_AUTHORITY = "com.example.unchilnote.fileprovider"

const val OPENWEATHER_URL = "https://api.openweathermap.org/data/2.5/"
const val OPENWEATHER_KEY = ""
const val OPENWEATHER_UNITS = "metric"
const val OPENWEATHER_RESULT_OK = 200


const  val TAG_M_KM = 1000
const val yyyyMMddHHmmE = "yyyy/MM/dd HH:mm E"
const val yyyyMMddHHmmssE = "yyyy/MM/dd HH:mm:ss E"
const val EEEHHmmss = "EEE HH:mm:ss"
const val WEATHER_TEXT_SUN = "Sunrise: %s Sunset: %s"
const val WEATHER_TEXT_TEMP = "Temp: %,.0f°C Min: %,.0f°C Max: %,.0f°C"
const val WEATHER_TEXT_WEATHER = "Pressure: %,.0fhPa Humidity: %,.0f"
const val WEATHER_TEXT_WIND = "Wind: %,.0fm/s Deg: %,.0f° Visibility: %dkm"

const val ATTACH_CNT_FORMAT = "Attach : Snapshot[%d] Record[%d] Photo[%d]"


/** Milliseconds used for UI animations */
const val ANIMATION_FAST_MILLIS = 50L
const val ANIMATION_SLOW_MILLIS = 100L

const val PLAYTIME_FORMAT = "mm:ss"
const val FILENAME_FORMAT = "yyyyMMdd-HHmmssSSS"
const val HHmmss = "HH:mm:ss"

const val MILLISEC_CHECK = 9999999999
const val MILLISEC_DIGIT = 1L
const val MILLISEC_CONV_DIGIT = 1000L
const val GAP_COLLECT_LOCATION_MIN = 60000  // 60 Sec
const val GAP_COLLECT_WEATHER_MIN = 300000 // 5 Min



const val EXTENSION_PHOTO = "_photo.jpg"
const val EXTENSION_MAP = "_map.jpg"
const val EXTENSION_RECORD = "_record.ogg"
const val JPG_CHECK = "JPG"
const val OGG_CHECK = "OGG"

const val THUMBNAIL_IMG_SIZE_W = 348
const val THUMBNAIL_IMG_SIZE_H = 348

const val LIST_IMG_SIZE_W = 1200
const val LIST_IMG_SIZE_H = 600


const val RATIO_4_3_VALUE = 4.0 / 3.0
const val RATIO_16_9_VALUE = 16.0 / 9.0

const val AUTO_CANCEL_DURATION = 5000L


const val PUBLIC_CALLER = "PUBLIC"
const val WEATHER_CALLER = "WEATHER"
const val TAG_CALLER = "TAGS"
const val ERROR_IMAGE = "ERROR"
const val LOADING_IMAGE = "LOADING"
const val ALERT_IMAGE = "ALERT"
const val EMPTY_IMAGE = "EMPTY"
const val DELETE_IMAGE = "DELETE"
const val DRAWING_IMAGE = "DRAWING"
const val SNAPSHOT_IMAGE = "SNAPSHOT"
const val MIC_ON_IMAGE = "MIC_ON"
const val MIC_OFF_IMAGE = "MIC_OFF"
const val PAUSE_IMAGE = "PAUSE"
const val PLAY_IMAGE = "PLAY"
const val LABEL_IMAGE = "LABEL"
const val SEARCH_IMAGE = "SEARCH"
const val SECRET_LOCK_IMAGE = "SECRET_LOCK"
const val SECRET_OPEN_IMAGE = "SECRET_OPEN"
const val PIN_ON_IMAGE = "PIN_ON"
const val PIN_OFF_IMAGE = "PIN_OFF"





const val SNAPSHOT_DEFAULT = "default"
const val SNAPSHOT_DRAWING = "drawing"

const val WRITE = "Write"
const val SAVE = "Save"
const val UPDATE = "Update"
const val LABEL = "Label"
const val LIST = "List"
const val WRITE_MENU = "WriteMenu"
const val LIST_MENU = "ListMenu"
const val DETAIL_MENU = "DetailMenu"
const val MAP_LAYER_MENU = "MapLayerMenu"
const val GOOGLE_MAP = "GoogleMap"


const val TAB_MAP_INDEX = 0
const val TAB_RECORD_INDEX = 1
const val TAB_PHOTO_INDEX = 2


const val OK = "Ok"
const val CANCEL = "Cancel"

const val FLASH_ON_IMAGE = "FLASH_ON"
const val FLASH_OFF_IMAGE = "FLASH_OFF"
const val FLASH_NO_IMAGE = "FLASH_NO"

// Argument Key Define
const val ARG_ROOTDIR = "root_dir"
const val ARG_FILENAME = "file_name"
const val ARG_TEXT = "text"
const val ARG_CALLER = "callerFragment"
