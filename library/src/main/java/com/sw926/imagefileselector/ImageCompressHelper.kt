package com.sw926.imagefileselector

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.AsyncTask
import java.io.File
import java.util.concurrent.Executors


class ImageCompressHelper {

    private var mCallback: Callback? = null

    @SuppressLint("ParcelCreator")
    data class CompressParams(
            var outputPath: String? = null,
            var maxWidth: Int = 1000,
            var maxHeight: Int = 1000,
            var saveQuality: Int = 80,
            var compressFormat: Bitmap.CompressFormat? = null
    )

    @SuppressLint("ParcelCreator")
    class CompressJop(
            var inputFile: String,
            var params: CompressParams = CompressParams(),
            var deleteInputFile: Boolean = false
    )

    fun setCallback(callback: Callback) {
        mCallback = callback
    }

    fun compress(jop: CompressJop) {
        CompressTask().executeOnExecutor(Executors.newCachedThreadPool(), jop)
    }

    private inner class CompressTask : AsyncTask<CompressJop, Int, String>() {

        override fun doInBackground(vararg jops: CompressJop): String? {
            AppLogger.i(TAG, "------------------ start compress file ------------------")
            val jop = jops[0]
            val param = jop.params

            val format = if (param.compressFormat == null) CompressFormatUtils.parseFormat(jop.inputFile) else param.compressFormat
            AppLogger.i(TAG, "use compress format:" + format?.name)

            val outputPath = param.outputPath
            if (outputPath == null) {
                clearJop(jop)
                return null
            }

            val parentDir = File(outputPath)
            if (!parentDir.exists()) {
                parentDir.mkdirs()
            }

            val outputFile = File(parentDir, "img_" + System.currentTimeMillis() + CompressFormatUtils.getExt(format))
            val bitmap = ImageUtils.compressImageFile(jop.inputFile, param.maxWidth, param.maxHeight)
            if (bitmap != null) {
                ImageUtils.saveBitmap(bitmap, outputFile.path, format, param.saveQuality)
                if (outputFile.exists()) {
                    AppLogger.i(TAG, "compress success, output file: " + outputFile.path)
                    clearJop(jop)
                    return outputFile.path
                }
            }
            clearJop(jop)
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result != null) {
                mCallback?.onSuccess(result)
            } else {
                mCallback?.onError()
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


    interface Callback {
        fun onError()

        fun onSuccess(file: String)
    }

    companion object {

        const val TAG = "ImageCompressHelper"
    }

}
