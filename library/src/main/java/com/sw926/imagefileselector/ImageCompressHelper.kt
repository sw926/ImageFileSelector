package com.sw926.imagefileselector

import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Build
import android.os.Parcel
import android.os.Parcelable

import java.io.File
import java.util.concurrent.Executors


class ImageCompressHelper {

    private var mCallback: Callback? = null

    class CompressParams : Parcelable {
        var outputPath: String? = null
        var maxWidth = 1000
        var maxHeight = 1000
        var saveQuality = 80
        var compressFormat: Bitmap.CompressFormat? = null

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(this.outputPath)
            dest.writeInt(this.maxWidth)
            dest.writeInt(this.maxHeight)
            dest.writeInt(this.saveQuality)
            dest.writeInt(this.compressFormat?.ordinal ?: -1)
        }

        constructor() {}

        public constructor(source: Parcel) {
            this.outputPath = source.readString()
            this.maxWidth = source.readInt()
            this.maxHeight = source.readInt()
            this.saveQuality = source.readInt()
            val tmpCompressFormat = source.readInt()
            this.compressFormat = if (tmpCompressFormat == -1) null else Bitmap.CompressFormat.values()[tmpCompressFormat]
        }

        companion object {

            val CREATOR: Parcelable.Creator<CompressParams> = object : Parcelable.Creator<CompressParams> {
                override fun createFromParcel(source: Parcel): CompressParams {
                    return CompressParams(source)
                }

                override fun newArray(size: Int): Array<CompressParams?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }


    class CompressJop : Parcelable {
        var inputFile: String
        var params: CompressParams
        var deleteInputFile = false

        constructor(params: CompressParams, inputFile: String) {
            this.params = params
            this.inputFile = inputFile
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(this.inputFile)
            dest.writeParcelable(this.params, flags)
            dest.writeByte(if (this.deleteInputFile) 1.toByte() else 0.toByte())
        }

        public constructor(source: Parcel) {
            this.inputFile = source.readString()
            this.params = source.readParcelable(CompressParams::class.java.classLoader)
            this.deleteInputFile = source.readByte().toInt() != 0
        }

        companion object {

            val CREATOR: Parcelable.Creator<CompressJop> = object : Parcelable.Creator<CompressJop> {
                override fun createFromParcel(source: Parcel): CompressJop {
                    return CompressJop(source)
                }

                override fun newArray(size: Int): Array<CompressJop?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    fun setCallback(callback: Callback) {
        mCallback = callback
    }

    fun compress(jop: CompressJop) {
        if (Build.VERSION.SDK_INT >= 11) {
            CompressTask().executeOnExecutor(Executors.newCachedThreadPool(), jop)
        } else {
            CompressTask().execute(jop)
        }
    }

    private inner class CompressTask : AsyncTask<CompressJop, Int, String>() {

        override fun doInBackground(vararg jops: CompressJop): String? {
            AppLogger.i(TAG, "------------------ start compress file ------------------")
            val jop = jops[0]
            val param = jop.params

            val format = if (param.compressFormat == null) CompressFormatUtils.parseFormat(jop.inputFile) else param.compressFormat
            AppLogger.i(TAG, "use compress format:" + format?.name)

            if (param.outputPath == null) {
                clearJop(jop)
                return null
            }

            val parentDir = File(param.outputPath)
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
