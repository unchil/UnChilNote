package com.example.unchilnote.utils

import android.annotation.SuppressLint
import android.content.res.Resources
import android.icu.text.SimpleDateFormat
import android.location.Location
import com.example.unchilnote.R
import com.example.unchilnote.data.dataset.*
import com.google.android.gms.maps.model.LatLng

class ConvFormat {
    companion object {

        fun makeRecordText(fileName : String): String {
            return "[${fileName.substring(  9 , 11)}:${fileName.substring(  11 , 13)}:${fileName.substring(  13 , 15)}]\n"
        }

        fun makeLabelArray(): Array<String> {
            return  arrayListOf<String>().apply {
                TAG_INFO.forEach {
                    this.add(it.key)
                }
            }.toTypedArray()
        }

        fun makeLabelValueArray(): BooleanArray {
            return  arrayListOf<Boolean>().apply {
                TAG_INFO.forEach {
                    this.add(it.value)
                }
            }.toBooleanArray()
        }

        fun MutableMap<String, Boolean>.toSelectedKeyString() : String {
            var result = ""
            this.filterValues {
                it
            }.forEach {
                result = result  + it.key + " "
            }
            return result
        }

        fun MutableMap<String, Boolean>.toValueString() : String {
            var result = ""
            forEach {
                when(it.value) {
                    true ->  result = result  + "1"
                    false -> result = result + "0"
                }
            }
            return result
        }

        fun MutableMap<String, Boolean>.toMEMO_TAG_TBL(id: Long): MEMO_TAG_TBL {
            return  MEMO_TAG_TBL(
                id = id,
                climbing = this[CLIMBING]?:false,
                tracking = this[TRACKING]?:false,
                camping =  this[CAMPING]?:false,
                travel =  this[TRAVEL]?:false,
                culture = this[CULTURE]?:false,
                art = this[ART]?:false,
                traffic = this[TRAFFIC]?:false,
                restaurant = this[RESTAURANT]?:false
            )
        }



        fun Location.toCurrentLocation_TBL(): CURRENTLOCATION_TBL {
            return CURRENTLOCATION_TBL(
                dt = this.time,
                latitude = this.latitude.toFloat(),
                longitude = this.longitude.toFloat(),
                altitude = this.altitude.toFloat()
            )
        }


        @SuppressLint("SimpleDateFormat")
        fun UnixTimeToString(time: Long, format: String): String{
            val UNIXTIMETAG_SECTOMILI
                    = if( time > MILLISEC_CHECK) MILLISEC_DIGIT else MILLISEC_CONV_DIGIT

            return SimpleDateFormat(format)
                .format(time * UNIXTIMETAG_SECTOMILI )
                .toString()
        }


        @SuppressLint("SimpleDateFormat")
        fun makeMediaPlayerDurationTime(currentTime:Int, durationTime:Int ): String {
            return SimpleDateFormat(PLAYTIME_FORMAT).format(currentTime) + "/" +
                    SimpleDateFormat(PLAYTIME_FORMAT).format(durationTime)

        }


    }
}