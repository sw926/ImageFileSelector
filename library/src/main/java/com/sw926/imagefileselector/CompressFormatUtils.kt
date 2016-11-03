package com.sw926.imagefileselector

import android.graphics.Bitmap
import android.os.Build

/**
 * Created by sunwei on 15/11/16.
 */
object CompressFormatUtils {

    fun parseFormat(fileName: String): Bitmap.CompressFormat {

        val dotPos = fileName.lastIndexOf(".")
        if (dotPos <= 0) {
            return Bitmap.CompressFormat.JPEG
        }
        val ext = fileName.substring(dotPos + 1)
        if (ext.equals("jpg", ignoreCase = true) || ext.equals("jpeg", ignoreCase = true)) {
            return Bitmap.CompressFormat.JPEG
        }
        if (ext.equals("png", ignoreCase = true)) {
            return Bitmap.CompressFormat.PNG
        }
        // TODO ignore webp?
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            if (ext.equals("webp", ignoreCase = true)) {
//                return Bitmap.CompressFormat.WEBP
//            }
//        }
        return Bitmap.CompressFormat.JPEG
    }

    fun getExt(format: Bitmap.CompressFormat): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (format == Bitmap.CompressFormat.WEBP) {
                return ".webp"
            }
        }

        if (format == Bitmap.CompressFormat.PNG) {
            return ".png"
        }
        return ".jpg"
    }

    fun getExt(fileName: String): String {
        val format = parseFormat(fileName)
        return getExt(format)
    }
}
