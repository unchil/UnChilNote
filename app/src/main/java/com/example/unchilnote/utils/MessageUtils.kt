package com.example.unchilnote.utils

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.example.unchilnote.BuildConfig
import com.google.android.material.snackbar.Snackbar

class MessageUtils {

    companion object {

        fun msgToSnackBar( view: View, title:String?, message: String, duration: Int , isAction: Boolean ) {

            val snackbar = Snackbar.make(view, message, duration)
            if (isAction) {
                snackbar.setAction(title) {
                    it.context.startActivity(
                        Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    )
                }
            }
/*
            val params: FrameLayout.LayoutParams  = snackbar.view.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            snackbar.view.layoutParams = params

 */
            snackbar.show()

        }





    }// companion


}