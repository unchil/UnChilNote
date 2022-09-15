package com.example.unchilnote


import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private val logTag = MainActivity::class.java.name
    private lateinit var permissionManager: PermissionManager
    private var isReqPermissionCheck:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)

        Log.d(logTag, "MainActivity onCreate.....")

        permissionManager = PermissionManager().apply {
            reqPermissionResultCode = Thread.currentThread().hashCode()
            permissionArray = resources.getStringArray(R.array.Req_Permission_Array)
            this@MainActivity.isReqPermissionCheck = permissionArray.isNotEmpty()
        }.also {
            if (isReqPermissionCheck) {
                if(it.checkPermissions(this@MainActivity)){
                    setContentView(R.layout.activity_main)
                }
            }
            it.chkBiometric(this@MainActivity.applicationContext)
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        setContentView(R.layout.activity_main)
        permissionManager.requestPermissionsResultProcess(requestCode, permissions, grantResults)
    }


}