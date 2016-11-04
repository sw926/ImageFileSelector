package com.sw926.imagefileselector

import android.graphics.Bitmap
import android.os.AsyncTask
import java.io.File
import java.util.concurrent.Executors

data class CompressParams(var outputPath: String,
                          var maxWidth: Int = 1000,
                          var maxHeight: Int = 1000,
                          var saveQuality: Int = 80,
                          var compressFormat: Bitmap.CompressFormat? = null)

data class CompressJop(var inputFile: String, val params: CompressParams)

class ImageCompressHelper {

    var errorCallback: (() -> Unit)? = null
    var successCallback: ((String) -> Unit)? = null

    fun compress(jop: CompressJop) {
        CompressTask().executeOnExecutor(Executors.newCachedThreadPool(), jop)
    }

    private inner class CompressTask : AsyncTask<CompressJop, Int, String>() {

        override fun doInBackground(vararg params: CompressJop): String? {
            AppLogger.i(TAG, "------------------ start compress file ------------------")
            val jop = params[0]
            val param = jop.params

            val format: Bitmap.CompressFormat = param.compressFormat ?: CompressFormatUtils.parseFormat(jop.inputFile)
            AppLogger.i(TAG, "use compress format:" + format.name)

            val parentDir = File(param.outputPath)
            if (!parentDir.exists()) {
                parentDir.mkdirs()
            }

            val outputFile = File(parentDir, "img_${System.currentTimeMillis()}${CompressFormatUtils.getExt(format)}")
            val bitmap: Bitmap? = ImageUtils.compressImageFile(jop.inputFile, param.maxWidth, param.maxHeight)
            bitmap?.let {
                ImageUtils.saveBitmap(bitmap, outputFile.path, format, param.saveQuality)
                if (outputFile.exists()) {
                    AppLogger.i(TAG, "compress success, output file: ${outputFile.path}")
                    return outputFile.path
                }
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result != null) {
                successCallback?.invoke(result)
            } else {
                errorCallback?.invoke()
            }
        }

    }

    companion object {

        val TAG: String = ImageCompressHelper::class.java.simpleName

    }
}
