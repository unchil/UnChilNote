package com.example.unchilnote.utils

import android.annotation.SuppressLint
import android.content.Context
import com.example.unchilnote.R
import com.example.unchilnote.camera.CameraFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup

class ResourceUtils {

    companion object {


        fun getResourceID( caller: String, type: String): Int {

            val errOutLine = R.drawable.outline_error_outline_24

            return when(caller) {
                PUBLIC_CALLER -> {
                    when (type) {
                        ERROR_IMAGE -> R.drawable.ic_broken_image
                        LOADING_IMAGE -> R.drawable.loading_img
                        ALERT_IMAGE -> R.drawable.baseline_warning_amber_24
                        EMPTY_IMAGE -> R.drawable.baseline_android_24
                        DELETE_IMAGE -> R.drawable.baseline_delete_24
                        DRAWING_IMAGE -> R.drawable.baseline_border_color_24
                        SNAPSHOT_IMAGE -> R.drawable.baseline_add_photo_alternate_24
                        MIC_ON_IMAGE -> R.drawable.baseline_mic_none_24
                        MIC_OFF_IMAGE -> R.drawable.baseline_mic_off_24
                        PLAY_IMAGE -> R.drawable.baseline_play_arrow_24
                        PAUSE_IMAGE -> R.drawable.baseline_pause_24
                        LABEL_IMAGE -> R.drawable.baseline_local_offer_24
                        SEARCH_IMAGE -> R.drawable.baseline_manage_search_24
                        LIST -> R.drawable.baseline_list_alt_24
                        WRITE -> R.drawable.baseline_mode_edit_outline_24
                        UPDATE -> R.drawable.baseline_done_all_24
                        SAVE -> R.drawable.baseline_save_24
                        CHECK -> R.drawable.baseline_check_circle_24
                        WRITE_MENU -> R.menu.menu_write
                        LIST_MENU -> R.menu.menu_list
                        DETAIL_MENU -> R.menu.memu_detail
                        MAP_LAYER_MENU -> R.menu.menu_map_layer
                        SECRET_OPEN_IMAGE -> R.drawable.baseline_lock_open_24
                        SECRET_LOCK_IMAGE -> R.drawable.baseline_lock_24
                        PIN_OFF_IMAGE -> R.drawable.baseline_add_location_alt_24
                        PIN_ON_IMAGE -> R.drawable.baseline_pin_drop_24
                        else -> errOutLine
                    }
                }
                TAG_CALLER -> {
                    when (type) {
                        CLIMBING -> R.drawable.baseline_landscape_24
                        TRACKING -> R.drawable.baseline_hiking_24
                        CAMPING -> R.drawable.outline_forest_24
                        TRAVEL -> R.drawable.baseline_flight_takeoff_24
                        CULTURE -> R.drawable.baseline_theaters_24
                        ART -> R.drawable.baseline_widgets_24
                        TRAFFIC -> R.drawable.baseline_traffic_24
                        RESTAURANT -> R.drawable.baseline_restaurant_24
                        else -> errOutLine
                    }
                }
                WEATHER_CALLER -> {
                    when(type){
                        "01d" -> R.drawable.ic_openweather_01d
                        "01n" -> R.drawable.ic_openweather_01n
                        "02d" -> R.drawable.ic_openweather_02d
                        "02n" -> R.drawable.ic_openweather_02n
                        "03d" -> R.drawable.ic_openweather_03d
                        "03n" -> R.drawable.ic_openweather_03n
                        "04d" -> R.drawable.ic_openweather_04d
                        "04n" -> R.drawable.ic_openweather_04n
                        "09d" -> R.drawable.ic_openweather_09d
                        "09n" -> R.drawable.ic_openweather_09n
                        "10d" -> R.drawable.ic_openweather_10d
                        "10n" -> R.drawable.ic_openweather_10n
                        "11d" -> R.drawable.ic_openweather_11d
                        "11n" -> R.drawable.ic_openweather_11n
                        "13d" -> R.drawable.ic_openweather_13d
                        "13n" -> R.drawable.ic_openweather_13n
                        "50d" -> R.drawable.ic_openweather_50d
                        "50n" -> R.drawable.ic_openweather_50n
                        else -> R.drawable.ic_openweather_unknown
                    }
                }

                CameraFragment::onClickFlash.name -> {
                    when(type) {
                        FLASH_OFF_IMAGE -> R.drawable.baseline_flash_off_24
                        FLASH_ON_IMAGE -> R.drawable.baseline_flash_on_24
                        FLASH_NO_IMAGE -> R.drawable.sharp_no_flash_24
                        else -> errOutLine
                    }
                }
                else -> errOutLine
            }
        }



        @SuppressLint("PrivateResource", "ResourceAsColor")
        fun createTagChips (chipGroup: ChipGroup, context: Context) {
            TAG_INFO.onEachIndexed { index, entry ->
                chipGroup.addView(
                    Chip(context).apply {

                        this.setChipDrawable(
                            ChipDrawable.createFromAttributes(
                                context, null, 0, com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice )
                        )

                        this.id = index
                        this.text = entry.key
                        this.isCheckable = true
                        this.isChipIconVisible = true
                        this.isCheckedIconVisible = false
                        this.isCloseIconVisible = false

                        this.setCheckedIconResource(com.google.android.material.R.drawable.ic_mtrl_chip_checked_black )
                        this.setTextColor(com.google.android.material.R.color.mtrl_choice_chip_text_color)
                  //      this.setBackgroundColor( com.google.android.material.R.color.mtrl_choice_chip_background_color )
                        this.setRippleColorResource(com.google.android.material.R.color.mtrl_choice_chip_ripple_color)

                        val id:Int = when(entry.key) {
                            CLIMBING -> getResourceID(TAG_CALLER, CLIMBING)
                            TRACKING -> getResourceID(TAG_CALLER, TRACKING)
                            CAMPING -> getResourceID(TAG_CALLER, CAMPING)
                            TRAVEL -> getResourceID(TAG_CALLER, TRAVEL)
                            CULTURE -> getResourceID(TAG_CALLER, CULTURE)
                            ART -> getResourceID(TAG_CALLER, ART)
                            TRAFFIC -> getResourceID(TAG_CALLER, TRAFFIC)
                            RESTAURANT -> getResourceID(TAG_CALLER, RESTAURANT)
                            else -> getResourceID(TAG_CALLER, "")
                        }
                        this.setChipIconResource(id)
                    }
                )
            }
        }



    }



}