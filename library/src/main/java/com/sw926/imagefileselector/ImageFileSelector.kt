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
    private val mImageTaker: ImageCaptureHelper
    private val mImageCompressHelper: ImageCompressHelper

    init {

        mImageCompressHelper = ImageCompressHelper(context)
        mImageCompressHelper.setCallback { outFile ->
//            AppLogger.d(TAG, "compress image output: " + outFile)
//            if (mCallback != null) {
//                mCallback!!.onSuccess(outFile)
//            }
        }

        val errorCallback: (ErrorResult) -> Unit = {
            mCallback?.onError(it)
        }

        val successCallback: (String) -> Unit = {
            mCallback?.onSuccess(it)
        }

        mImagePickHelper = ImagePickHelper(context)
        mImagePickHelper.errorCallback = errorCallback
        mImagePickHelper.successCallback = successCallback


        mImageTaker = ImageCaptureHelper()
        mImageTaker.errorCallback = errorCallback
        mImageTaker.successCallback = successCallback

    }

    fun setOutPutPath(outPutPath: String) {
        val outPutPath1 = outPutPath
        mImageCompressHelper.setOutputPath(outPutPath)
    }

    fun setMaxOutputFileLength(length: Long) {
        mImageCompressHelper.setMaxOutputFileLength(length)
    }

    /**
     * 设置压缩后的文件大小

     * @param maxWidth  压缩后文件宽度
     * *
     * @param maxHeight 压缩后文件高度
     */
    @SuppressWarnings("unused")
    fun setOutPutImageSize(maxWidth: Int, maxHeight: Int) {
        mImageCompressHelper.setOutPutImageSize(maxWidth, maxHeight)
    }

    /**
     * 设置压缩后保存图片的质量

     * @param quality 图片质量 0 - 100
     */
    @SuppressWarnings("unused")
    fun setQuality(quality: Int) {
        mImageCompressHelper.setQuality(quality)
    }

    /**
     * set image compress format

     * @param compressFormat compress format
     */
    @SuppressWarnings("unused")
    fun setCompressFormat(compressFormat: Bitmap.CompressFormat) {
        mImageCompressHelper.setCompressFormat(compressFormat)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mImagePickHelper.onActivityResult(requestCode, resultCode, data)
        mImageTaker.onActivityResult(requestCode, resultCode, data)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == mImagePickHelper.mRequestCode) {
            mImagePickHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
        } else if (requestCode == mImageTaker.mRequestCode) {
            mImageTaker.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
        mImageTaker.captureImage(activity, requestCode)
    }

    fun takePhoto(fragment: android.support.v4.app.Fragment, requestCode: Int) {
        mImageTaker.captureImage(fragment, requestCode)
    }

    interface Callback {
        fun onError(errorResult: ErrorResult)
        fun onSuccess(file: String)
    }

    companion object {

        private val TAG = ImageFileSelector::class.java.simpleName

        fun setDebug(debug: Boolean) {
            AppLogger.DEBUG = debug
        }
    }

}
