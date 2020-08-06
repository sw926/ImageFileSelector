package com.sw926.imagefileselector

import android.annotation.SuppressLint
import android.graphics.Bitmap
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File

object ImageCompressHelper {
    const val TAG = "ImageCompressHelper"

    @SuppressLint("ParcelCreator")
    data class CompressParams(
            val outputPath: String? = null,
            val maxWidth: Int = 1000,
            val maxHeight: Int = 1000,
            val saveQuality: Int = 80,
            val compressFormat: Bitmap.CompressFormat? = null
    )

    @SuppressLint("ParcelCreator")
    class CompressJop(
            val inputFile: String,
            val params: CompressParams = CompressParams(),
            val deleteInputFile: Boolean = false
    )


    fun compress(jop: CompressJop, callback: (String?) -> Unit) {
        val param = jop.params
        val outputPath = param.outputPath
        if (outputPath == null) {
            clearJop(jop)
            callback(null)
            return
        }

        val format = param.compressFormat ?: CompressFormatUtils.parseFormat(jop.inputFile)
        doAsync {
            val parentDir = File(outputPath)
            if (!parentDir.exists()) {
                parentDir.mkdirs()
            }
            val outputFile = File(parentDir, "img_" + System.currentTimeMillis() + CompressFormatUtils.getExt(format))
            val bitmap = ImageUtils.compressImageFile(jop.inputFile, param.maxWidth, param.maxHeight)
            if (bitmap != null) {
                ImageUtils.saveBitmap(bitmap, outputFile.path, format, param.saveQuality)
            }
            if (outputFile.exists()) {
                clearJop(jop)
                uiThread {
                    callback.invoke(outputFile.path)
                }
            } else {
                uiThread {
                    callback.invoke(null)
                }
            }
        }
    }

    /**
     * clear temp file when job finished
     *
     * @param jop
     */
    private fun clearJop(jop: CompressJop) {
        if (jop.deleteInputFile) {
            val file = File(jop.inputFile)
            if (file.exists()) {
                AppLogger.w(TAG, "delete input file: " + file.absolutePath)
                file.delete()
            }
        }
    }

}
