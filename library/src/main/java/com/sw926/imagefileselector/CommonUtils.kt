package com.sw926.imagefileselector

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.*

object CommonUtils {

    fun getOutPutPath(context: Context): File {
        return File(context.externalCacheDir, "/imagefileselector/")
    }

    fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, getFileProviderName(context), file)
    }

    private fun getFileProviderName(context: Context): String {
        return context.packageName + ".com.sw926.imagefileselector.provider"
    }

    fun generateImageCacheFile(context: Context, ext: String): File {
        val fileName = "img_${System.currentTimeMillis()}$ext"
        return File(getOutPutPath(context), fileName)
    }

    fun hasSDCardMounted(): Boolean {
        val state = Environment.getExternalStorageState()
        return state != null && state == Environment.MEDIA_MOUNTED
    }
}