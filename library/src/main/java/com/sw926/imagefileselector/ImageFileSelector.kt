@file:JvmName("ImageFileSelector")

package com.sw926.imagefileselector

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import java.io.File

class ImageFileSelector(context: Context) {

    companion object {

        private const val TAG = "ImageFileSelector"

        fun setDebug(debug: Boolean) {
            AppLogger.DEBUG = debug
        }
    }

    private var mCallback: Callback? = null
    private val mImagePickHelper: ImagePickHelper
    private val mImageCaptureHelper: ImageCaptureHelper

    private var compressParams: ImageCompressHelper.CompressParams

    init {
        compressParams = ImageCompressHelper.CompressParams(outputPath = CommonUtils.getOutPutPath(context).absolutePath)

        mImagePickHelper = ImagePickHelper()
        mImagePickHelper.setCallback(ImageGetCallback(false))

        mImageCaptureHelper = ImageCaptureHelper()
        mImageCaptureHelper.setCallback(ImageGetCallback(true))
    }


    @Suppress("unused")
    fun setOutPutPath(outPutPath: String) {
        compressParams = compressParams.copy(outputPath = outPutPath)
    }

    @Suppress("unused")
    fun setSelectFileType(type: String) {
        mImagePickHelper.setType(type)
    }

    /**
     * 设置压缩后的文件大小
     *
     * @param maxWidth  压缩后文件宽度
     * *
     * @param maxHeight 压缩后文件高度
     */
    fun setOutPutImageSize(maxWidth: Int, maxHeight: Int) {
        compressParams = compressParams.copy(maxWidth = maxWidth, maxHeight = maxHeight)
    }

    /**
     * 设置压缩后保存图片的质量
     *
     * @param quality 图片质量 0 - 100
     */
    @Suppress("unused")
    fun setQuality(quality: Int) {
        compressParams = compressParams.copy(saveQuality = quality)
    }

    /**
     * set image compress format
     *
     * @param compressFormat compress format
     */
    @Suppress("unused")
    fun setCompressFormat(compressFormat: Bitmap.CompressFormat) {
        compressParams = compressParams.copy(compressFormat = compressFormat)
    }

    fun onActivityResult(context: Context, requestCode: Int, resultCode: Int, data: Intent?) {
        mImagePickHelper.onActivityResult(requestCode, resultCode, data)
        mImageCaptureHelper.onActivityResult(context, requestCode, resultCode, data)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            mImagePickHelper.requestCode -> {
                mImagePickHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
            mImageCaptureHelper.requestCode -> {
                mImageCaptureHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        mImageCaptureHelper.onSaveInstanceState(outState)
        mImagePickHelper.onSaveInstanceState(outState)
    }

    fun onRestoreInstanceState(outState: Bundle?) {
        mImageCaptureHelper.onRestoreInstanceState(outState)
        mImagePickHelper.onRestoreInstanceState(outState)
    }

    fun setCallback(callback: Callback?) {
        mCallback = callback
    }

    fun selectImage(activity: Activity, requestCode: Int) {
        mImagePickHelper.selectorImage(activity, requestCode)
    }

    fun selectImage(fragment: Fragment, requestCode: Int) {
        mImagePickHelper.selectImage(fragment, requestCode)
    }

    fun takePhoto(activity: Activity, requestCode: Int) {
        mImageCaptureHelper.captureImage(activity, requestCode)
    }

    fun takePhoto(fragment: Fragment, requestCode: Int) {
        mImageCaptureHelper.captureImage(fragment, requestCode)
    }

    private inner class ImageGetCallback constructor(private val mDeleteOutputFile: Boolean) : Callback {

        override fun onError(errorResult: ErrorResult) {
            mCallback?.onError(errorResult)
        }

        override fun onSuccess(file: String) {
            AppLogger.d(TAG, "get file: $file")
            val jop = ImageCompressHelper.CompressJop(
                    inputFile = file,
                    params = compressParams,
                    deleteInputFile = mDeleteOutputFile
            )
            ImageCompressHelper.compress(jop) {
                if (it != null) {
                    mCallback?.onSuccess(it)
                } else {
                    mCallback?.onError(ErrorResult.error)
                }
            }
        }
    }

    interface Callback {
        fun onError(errorResult: ErrorResult)

        fun onSuccess(file: String)
    }

}
