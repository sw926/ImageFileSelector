package com.sw926.imagefileselector

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle

@SuppressWarnings("unused")
class ImageFileSelector(context: Context) {

    private var mCallback: Callback? = null
    private val mImagePickHelper: ImagePickHelper
    private val mImageCaptureHelper: ImageCaptureHelper
    private val mImageCompressHelper: ImageCompressHelper

    private val compressParams: CompressParams

    init {

        val defaultOutputPath = "${context.cacheDir}/images/"
        compressParams = CompressParams(defaultOutputPath)

        mImageCompressHelper = ImageCompressHelper()
        mImageCompressHelper.errorCallback = {
            mCallback?.onError(ErrorResult.error)
        }

        mImageCompressHelper.successCallback = {
            mCallback?.onSuccess(it)
        }

        val errorCallback: (ErrorResult) -> Unit = {
            mCallback?.onError(it)
        }

        val successCallback: (String) -> Unit = {
            mImageCompressHelper.compress(CompressJop(it, compressParams))
        }

        mImagePickHelper = ImagePickHelper(context)
        mImagePickHelper.errorCallback = errorCallback
        mImagePickHelper.successCallback = successCallback


        mImageCaptureHelper = ImageCaptureHelper()
        mImageCaptureHelper.errorCallback = errorCallback
        mImageCaptureHelper.successCallback = successCallback

    }

    fun setOutPutPath(outPutPath: String) {
        val outPutPath1 = outPutPath
        compressParams.outputPath = outPutPath
    }


    /**
     * 设置压缩后的文件大小

     * @param maxWidth  压缩后文件宽度
     * *
     * @param maxHeight 压缩后文件高度
     */
    @SuppressWarnings("unused")
    fun setOutPutImageSize(maxWidth: Int, maxHeight: Int) {
        compressParams.maxWidth = maxWidth
        compressParams.maxHeight =maxHeight
    }

    /**
     * 设置压缩后保存图片的质量

     * @param quality 图片质量 0 - 100
     */
    @SuppressWarnings("unused")
    fun setQuality(quality: Int) {
        compressParams.saveQuality = quality
    }

    /**
     * set image compress format

     * @param compressFormat compress format
     */
    @SuppressWarnings("unused")
    fun setCompressFormat(compressFormat: Bitmap.CompressFormat) {
        compressParams.compressFormat = compressFormat
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mImagePickHelper.onActivityResult(requestCode, resultCode, data)
        mImageCaptureHelper.onActivityResult(requestCode, resultCode, data)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == mImagePickHelper.mRequestCode) {
            mImagePickHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
        } else if (requestCode == mImageCaptureHelper.mRequestCode) {
            mImageCaptureHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle) {
    }

    fun setCallback(callback: Callback) {
        mCallback = callback
    }

    fun selectImage(activity: Activity, requestCode: Int) {
        mImagePickHelper.selectorImage(activity, requestCode)
    }

    fun selectImage(fragment: android.support.v4.app.Fragment, requestCode: Int) {
        mImagePickHelper.selectImage(fragment, requestCode)
    }

    fun takePhoto(activity: Activity, requestCode: Int) {
        mImageCaptureHelper.captureImage(activity, requestCode)
    }

    fun takePhoto(fragment: android.support.v4.app.Fragment, requestCode: Int) {
        mImageCaptureHelper.captureImage(fragment, requestCode)
    }

    interface Callback {
        fun onError(errorResult: ErrorResult)
        fun onSuccess(file: String)
    }

    companion object {

        fun setDebug(debug: Boolean) {
            AppLogger.DEBUG = debug
        }
    }

}
