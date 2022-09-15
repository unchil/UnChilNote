package com.example.unchilnote

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.unchilnote.utils.MessageUtils
import com.google.android.material.snackbar.Snackbar

class PermissionManager() {
    private val logTag = PermissionManager::class.java.name
    var reqPermissionResultCode:Int = -1
    lateinit var permissionArray: Array<String>
    lateinit var activity: AppCompatActivity

    private fun approvedPermissions(context: Context): Boolean {
        permissionArray.forEach {
            if ( PackageManager.PERMISSION_DENIED ==
                    ActivityCompat.checkSelfPermission(context , it) ) return false
        }
        return true
    }

    private fun requestPermissions(activity: AppCompatActivity) {
        this.activity = activity
        ActivityCompat.requestPermissions (activity, permissionArray, reqPermissionResultCode)
    }

    fun checkPermissions(activity: AppCompatActivity) : Boolean {
       return  if(!approvedPermissions(activity.applicationContext)) {
            requestPermissions(activity)
            false
        }else {
            true
        }
    }

    fun requestPermissionsResultProcess( requestCode: Int,
                                         permissions: Array<out String>,
                                         grantResults: IntArray)  {
        Log.d(logTag, "requestPermissionsResultProcess Start")

        val context = activity.applicationContext

        if ( grantResults.isNotEmpty() &&
            requestCode == reqPermissionResultCode ) {

            permissions.forEachIndexed { index, s ->
                when(grantResults[index]) {
                    PackageManager.PERMISSION_DENIED -> {
                        Log.d(logTag,  "Request Permission Denied [${s}]")
                    }
                    PackageManager.PERMISSION_GRANTED -> {
                        Log.d(logTag,  "Request Permission Granted [${s}]")
                    }
                }
            }

            if(grantResults.contains(PackageManager.PERMISSION_DENIED)) {
                MessageUtils.msgToSnackBar(
                    activity.findViewById(R.id.activity_main),
                    context.getString(R.string.SnackBarTitle_GrantPermission),
                    context.getString(R.string.SnackBarMessage_GrantPermission_DENIED),
                    context.getString(R.string.SnackBarDuration_GrantPermission).toInt(),
                    true
                )
            } else {
                MessageUtils.msgToSnackBar(
                    activity.findViewById(R.id.activity_main),
                    null,
                    context.getString(R.string.SnackBarMessage_GrantPermission_GRANTED),
                    Snackbar.LENGTH_LONG,
                    false
                )
            }


        }
    }

    fun chkBiometric(context: Context) {

        val bioMetricResult: Int
            = BiometricManager
            .from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG
                    or BiometricManager.Authenticators.DEVICE_CREDENTIAL)

        when (bioMetricResult) {

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.d(logTag, context.getString(R.string.BIOMETRIC_ERROR_NONE_ENROLLED))

                // Prompts the user to create credentials that your app accepts.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val enrollIntent
                        = Intent( Settings.ACTION_BIOMETRIC_ENROLL )
                            .apply {
                                putExtra( Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                    BiometricManager.Authenticators.BIOMETRIC_STRONG
                                            or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                            }
                    startActivityForResult(activity, enrollIntent, reqPermissionResultCode, null)
                } else  Log.d(logTag, context.getString(R.string.Build_VERSION_CHECK))
            }

            BiometricManager.BIOMETRIC_SUCCESS ->
                Log.d(logTag, context.getString(R.string.BIOMETRIC_SUCCESS))

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.d(logTag, context.getString(R.string.BIOMETRIC_ERROR_NO_HARDWARE))

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.d(logTag, context.getString(R.string.BIOMETRIC_ERROR_HW_UNAVAILABLE))

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                Log.d(logTag, context.getString(R.string.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED))

            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                Log.d(logTag, context.getString(R.string.BIOMETRIC_ERROR_UNSUPPORTED))

            BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
                Log.d(logTag, context.getString(R.string.BIOMETRIC_STATUS_UNKNOWN))

        }
    }



}