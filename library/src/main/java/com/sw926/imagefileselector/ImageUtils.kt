package com.sw926.imagefileselector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors

object ImageUtils {

    private val executor = Executors.newSingleThreadExecutor()

    data class SampleSize(val inSampleSize: Int, val width: Int, val height: Int)

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

    private fun getImageSize(inputStream: InputStream): SampleSize {
        val option = BitmapFactory.Options()
        option.inJustDecodeBounds = true
        BitmapFactory.decodeStream(inputStream, null, option)
        return SampleSize(1, option.outWidth, option.outHeight)
    }

    private fun getExifOrientation(inputStream: InputStream): Int {
        var degree = 0
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
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

    fun getSampleSize(actualWidth: Int, actualHeight: Int, maxWidth: Int, maxHeight: Int): SampleSize {
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
        return SampleSize(sampleSize, w, h)
    }

    private fun Uri.openStream(context: Context): InputStream? {
        return when (scheme) {
            "content" -> context.contentResolver.openInputStream(this)
            "file" -> FileInputStream(path)
            else -> null
        }
    }

    fun compressImage(context: Context, uri: Uri, maxWidth: Int, maxHeight: Int): Bitmap? {
        val imageSize = uri.openStream(context)?.use { getImageSize(it) } ?: return null
        val degree = uri.openStream(context)?.use { getExifOrientation(it) } ?: return null

        val (w, h) = if (degree == 90 || degree == 270) {
            Pair(imageSize.height, imageSize.width)
        } else {
            Pair(imageSize.width, imageSize.height)
        }
        val sampleSize = getSampleSize(w, h, maxWidth, maxHeight)
        val bitmap = if (w <= maxWidth && h <= maxHeight) {
            uri.openStream(context)?.use { BitmapFactory.decodeStream(it) }
        } else {
            val options = BitmapFactory.Options()
            options.inSampleSize = sampleSize.inSampleSize
            uri.openStream(context)?.use { BitmapFactory.decodeStream(it, null, options) }

        }

        val tempBitmap = if (degree != 0) {
            bitmap?.let {
                createRotateImage(degree, bitmap).also {
                    bitmap.recycle()
                }
            }
        } else {
            bitmap
        }
        return if (tempBitmap != null && (tempBitmap.width > maxWidth || tempBitmap.height > maxHeight)) {
            Bitmap.createScaledBitmap(tempBitmap, sampleSize.width, sampleSize.height, true).also {
                tempBitmap.recycle()
            }
        } else {
            tempBitmap
        }
    }

    fun createRotateImage(angle: Int, bitmap: Bitmap): Bitmap? {
        return kotlin.runCatching {
            val matrix = Matrix()
            matrix.postRotate(angle.toFloat())
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }.onFailure {
            it.printStackTrace()
        }.getOrNull()

    }

    fun compress(context: Context, uri: Uri, compressParams: CompressParams, callback: (String?) -> Unit) {
        executor.execute {
            val bitmap = compressImage(context, uri, compressParams.maxWidth, compressParams.maxWidth)
            if (bitmap != null) {
                val file = CommonUtils.generateImageCacheFile(context, CompressFormatUtils.getExt(compressParams.compressFormat))
                saveBitmap(bitmap, filePath = file.path, compressParams.compressFormat, compressParams.saveQuality)
                ContextCompat.getMainExecutor(context).execute {
                    callback.invoke(file.path)
                }
            } else {
                ContextCompat.getMainExecutor(context).execute {
                    callback.invoke(null)
                }
            }
        }
    }
}