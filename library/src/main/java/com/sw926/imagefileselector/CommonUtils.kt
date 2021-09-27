package com.sw926.imagefileselector

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object CommonUtils {

    fun getOutPutPath(context: Context): File {
        val file = File(context.cacheDir, "/imagefileselector/")
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

    fun getPathFromFileProviderUri(context: Context, uri: Uri): String? {
        return uri.lastPathSegment?.let {
            File(getOutPutPath(context), it).path
        }
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

}