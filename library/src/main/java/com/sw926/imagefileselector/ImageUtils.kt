package com.sw926.imagefileselector

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageUtils {
    fun compressImageFile(srcFile: String, maxWidth: Int, maxHeight: Int): Bitmap? {
        var bitmap: Bitmap? = null
        val inputFileLength = File(srcFile).length()
        AppLogger.i(ImageCompressHelper.TAG, "compress file:$srcFile")
        AppLogger.i(ImageCompressHelper.TAG, "file length:" + inputFileLength / 1024.0 + "kb")
        AppLogger.i(ImageCompressHelper.TAG, "max output size:($maxWidth,$maxHeight")
        val decodeOptions = BitmapFactory.Options()
        decodeOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(srcFile, decodeOptions)
        val actualWidth = decodeOptions.outWidth
        val actualHeight = decodeOptions.outHeight
        AppLogger.i(ImageCompressHelper.TAG, "input size:($actualWidth, $actualHeight")
        if (actualHeight <= maxHeight && actualWidth <= maxWidth) {
            AppLogger.w(ImageCompressHelper.TAG, "no need to compress: input size < output size")
            decodeOptions.inJustDecodeBounds = false
            try {
                bitmap = BitmapFactory.decodeFile(srcFile)
            } catch (e: OutOfMemoryError) {
                AppLogger.printStackTrace(e)
                AppLogger.e(ImageCompressHelper.TAG, "OutOfMemoryError:$srcFile, size:$actualWidth,$actualHeight")
            }
            if (bitmap != null) {
                val degree = getExifOrientation(srcFile)
                if (degree != 0) {
                    AppLogger.w(ImageCompressHelper.TAG, "rotate image from: $degree")
                    bitmap = rotateImageNotNull(degree, bitmap)
                }
            }
            return bitmap
        }
        val sampleSize: Int
        val w: Int
        val h: Int
        if (actualWidth * maxHeight > maxWidth * actualHeight) {
            w = maxWidth
            h = (w * actualHeight / actualWidth.toDouble()).toInt()
            sampleSize = (actualWidth / maxWidth.toDouble()).toInt()
        } else {
            h = maxHeight
            w = (h * actualWidth / actualHeight.toDouble()).toInt()
            sampleSize = (actualHeight / maxHeight.toDouble()).toInt()
        }
        AppLogger.i(ImageCompressHelper.TAG, "in simple size:$sampleSize")
        decodeOptions.inJustDecodeBounds = false
        decodeOptions.inSampleSize = sampleSize
        decodeOptions.inPreferredConfig = Bitmap.Config.RGB_565
        decodeOptions.inPurgeable = true
        decodeOptions.inInputShareable = true
        try {
            bitmap = BitmapFactory.decodeFile(srcFile, decodeOptions)
        } catch (error: OutOfMemoryError) {
            error.printStackTrace()
            AppLogger.e(ImageCompressHelper.TAG, "OutOfMemoryError:$srcFile, size:$actualWidth,$actualHeight")
        }
        if (bitmap != null) {
            AppLogger.i(ImageCompressHelper.TAG, "bitmap size after decode:(" + bitmap.width + ", " + bitmap.height + ")")
            if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
                var tempBitmap: Bitmap? = null
                try {
                    tempBitmap = Bitmap.createScaledBitmap(bitmap, w, h, true)
                } catch (e: OutOfMemoryError) {
                    AppLogger.printStackTrace(e)
                }
                if (tempBitmap != null) {
                    bitmap.recycle()
                    bitmap = tempBitmap
                    AppLogger.i(ImageCompressHelper.TAG, "scale down:(" + bitmap.width + ", " + bitmap.height + ")")
                }
            }
            val degree = getExifOrientation(srcFile)
            if (degree != 0) {
                AppLogger.i(ImageCompressHelper.TAG, "rotate image from: $degree")
                bitmap = rotateImageNotNull(degree, bitmap)
            }
            AppLogger.i(ImageCompressHelper.TAG, "output file width: " + bitmap.width + ", height: " + bitmap.height)
        }
        return bitmap
    }

    fun saveBitmap(bmp: Bitmap, filePath: String, format: CompressFormat, quality: Int) {
        val file = File(filePath)
        if (file.parentFile?.exists() != true) {
            file.parentFile?.mkdirs()
        }
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        FileOutputStream(file, true).use {
            bmp.compress(format, quality, it)
            it.flush()
        }
    }

    fun getExifOrientation(filepath: String): Int {
        var degree = 0
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(filepath)
        } catch (e: IOException) {
            AppLogger.printStackTrace(e)
        }
        if (exif != null) {
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
            if (orientation != -1) {
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                }
            }
        }
        return degree
    }

    private fun rotateImageNotNull(angle: Int, bitmap: Bitmap): Bitmap {
        var out: Bitmap? = null
        try {
            val matrix = Matrix()
            matrix.postRotate(angle.toFloat())
            out = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: OutOfMemoryError) {
            AppLogger.printStackTrace(e)
            AppLogger.e(ImageCompressHelper.TAG, "rotate image error, image will not display in current orientation")
        }
        if (out != null) {
            bitmap.recycle()
            return out
        }
        return bitmap
    }
}