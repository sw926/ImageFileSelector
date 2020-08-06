package com.sw926.imagefileselector

import android.graphics.Bitmap.CompressFormat
import android.os.Build

/**
 * Created by sunwei on 15/11/16.
 */
object CompressFormatUtils {
    fun parseFormat(fileName: String): CompressFormat {
        val dotPos = fileName.lastIndexOf(".")
        if (dotPos <= 0) {
            return CompressFormat.JPEG
        }
        val ext = fileName.substring(dotPos + 1)
        if (ext.equals("jpg", ignoreCase = true) || ext.equals("jpeg", ignoreCase = true)) {
            return CompressFormat.JPEG
        }
        return if (ext.equals("png", ignoreCase = true)) {
            CompressFormat.PNG
        } else CompressFormat.JPEG
        // TODO ignore webp?
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            if (ext.equals("webp", ignoreCase = true)) {
//                return Bitmap.CompressFormat.WEBP
//            }
//        }
    }

    fun getExt(format: CompressFormat): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (format == CompressFormat.WEBP) {
                return ".webp"
            }
        }
        return if (format == CompressFormat.PNG) {
            ".png"
        } else ".jpg"
    }

    fun getExt(fileName: String): String {
        val format = parseFormat(fileName)
        return getExt(format)
    }
}