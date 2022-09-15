package com.example.unchilnote.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class FileUtils {



    companion object {


        fun moveAttachFile(context: Context, type: String ) {

            when(type) {
                DIR_DEFAULT -> {
                    getOutputDirectory(context, DIR_DEFAULT, isCache = true).listFiles { file ->
                        file.renameTo( File( getOutputDirectory(context, SNAPSHOT, isCache = false), file.name))
                    }
                }
                SNAPSHOT -> {
                    getOutputDirectory(context, SNAPSHOT, isCache = true).listFiles { file ->
                        file.renameTo( File( getOutputDirectory(context, SNAPSHOT, isCache = false), file.name))
                    }
                }
            }
            getOutputDirectory(context, PHOTO, isCache = true).listFiles { file ->
                file.renameTo( File( getOutputDirectory(context, PHOTO, isCache = false), file.name))
            }
            getOutputDirectory(context, RECORD, isCache = true).listFiles { file ->
                file.renameTo( File( getOutputDirectory(context, RECORD, isCache = false), file.name))
            }
        }

        fun clearDefaultFile(context: Context) {
            getOutputDirectory(context, DIR_DEFAULT, isCache = true).listFiles { file ->
                file.delete()
            }
        }

        fun clearCacheFile(context: Context) {
            getOutputDirectory(context, DIR_DEFAULT, isCache = true).listFiles { file ->
                file.delete()
            }
            getOutputDirectory(context, SNAPSHOT, isCache = true).listFiles { file ->
                file.delete()
            }
            getOutputDirectory(context, PHOTO, isCache = true).listFiles { file ->
                file.delete()
            }
            getOutputDirectory(context, RECORD, isCache = true).listFiles { file ->
                file.delete()
            }
        }


        fun getOutputDirectory(context: Context, dir:String, isCache: Boolean): File {

         var mediaDir: File? = null

         if( Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED ) {

             mediaDir = when (isCache) {
                 true -> {
                     context.externalCacheDir?.let {
                         File(it, dir)?.apply { mkdirs() }
                     }
                 }
                 false -> {
                     val environmentPathName = when (dir) {
                         PHOTO -> {
                             Environment.DIRECTORY_PICTURES
                         }
                         SNAPSHOT -> {
                             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                                 Environment.DIRECTORY_SCREENSHOTS
                             else Environment.DIRECTORY_PICTURES
                         }
                         RECORD -> {
                             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                                 Environment.DIRECTORY_RECORDINGS
                             else Environment.DIRECTORY_DOCUMENTS
                         }
                         else -> {
                             Environment.DIRECTORY_DOCUMENTS
                         }
                     }
                     context.getExternalFilesDir(environmentPathName)
                 }
             }

         }
         return if( mediaDir != null && mediaDir.exists()) mediaDir
                else context.filesDir
        }



        fun createFile(baseFolder: File, format:String, extension: String) =
             File( baseFolder,
                 SimpleDateFormat(format, Locale.US)
                        .format(System.currentTimeMillis()) + extension)



    }




}