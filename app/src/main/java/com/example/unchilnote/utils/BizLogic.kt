package com.example.unchilnote.utils

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.widget.LinearLayout
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.unchilnote.R
import com.example.unchilnote.list.ListFragmentDirections
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.ChipGroup
import com.google.maps.android.ktx.model.cameraPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class BizLogic {

    companion object {


        fun launchDataLoad(coroutineScope: CoroutineScope,
                           _progressBarVisible: MutableLiveData<Boolean>,
                           _messageLiveData: MutableLiveData<String?>,
                           block: suspend () -> Unit): Job {
            return coroutineScope.launch {
                try{
                    _progressBarVisible.value = true
                    block()
                }catch ( error: Throwable) {
                    _messageLiveData.value = error.message
                } finally {
                    _progressBarVisible.value = false
                }
            }
        }

        fun setTagsMap(chipGroup: ChipGroup): MutableMap<String,Boolean> {
            initTagsMap()
            chipGroup.checkedChipIds.forEach { id ->
                TAG_INFO.onEachIndexed { index, entry ->
                    if ( id == index) {
                        TAG_INFO[entry.key] = true
                        return@forEach
                    }
                }
            }
            return TAG_INFO
        }



       fun initTagsMap() {
            TAG_INFO.forEach { key, value ->
                TAG_INFO.replace(key, false)
            }
        }



        fun getBiometricPromptInfo(resources: Resources):  BiometricPrompt.PromptInfo {
            return BiometricPrompt.PromptInfo.Builder()
                .setTitle(resources.getString(R.string.BioMetric_Title))
                .setSubtitle(resources.getString(R.string.BioMetric_Subtitle))
                .setAllowedAuthenticators( BiometricManager.Authenticators.BIOMETRIC_STRONG
                        or BiometricManager.Authenticators.DEVICE_CREDENTIAL )
                .build()
        }


        fun addMarkerToMap(mMap: GoogleMap, location: LatLng){
/*
            mMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    cameraPosition {
                        target(location)
                        zoom(0.0f)
                        tilt(0.0f)
                        bearing(90f)
                    }
                )
            )
*/
            mMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    cameraPosition {
                        target(location)
                        zoom(14.0f)
                        tilt(75f)
                        bearing(0.0f)
                        build()
                    }
                )
            )

        }


        fun setBottomSheetBehavior(sheetBehavior: BottomSheetBehavior<LinearLayout>){


            sheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {

                override fun onStateChanged(bottomSheet: View, newState: Int) {

                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    when (slideOffset) {
                        -1.0F -> {
                            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                        0.0F -> {
                            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                        1.0F -> {
                            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                        else -> {
                            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                    }
                }

            })
        }



    }// companion object
}