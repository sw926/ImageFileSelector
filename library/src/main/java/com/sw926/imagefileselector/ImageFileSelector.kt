package com.sw926.imagefileselector

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import java.io.File

class ImageFileSelector(context: Context) {

    private var mCallback: Callback? = null
    private val mImagePickHelper: ImagePickHelper
    private val mImageCaptureHelper: ImageCaptureHelper
    private val mImageCompressHelper: ImageCompressHelper
    private val permissionsHelper = PermissionsHelper()

    var compressParams: ImageCompressHelper.CompressParams
    private val mDefaultOutputPath: String

    init {
        mDefaultOutputPath = context.externalCacheDir.toString() + "/images/"

        compressParams = ImageCompressHelper.CompressParams()
        compressParams.outputPath = mDefaultOutputPath

        mImageCompressHelper = ImageCompressHelper()
        mImageCompressHelper.setCallback(object : ImageCompressHelper.Callback {
            override fun onError() {
                mCallback?.onError(ErrorResult.error)
            }

            override fun onSuccess(file: String) {
                mCallback?.onSuccess(file)
            }
        })

        mImagePickHelper = ImagePickHelper(context)
        mImagePickHelper.setCallback(ImageGetCallback(false))

        mImageCaptureHelper = ImageCaptureHelper()
        mImageCaptureHelper.setCallback(ImageGetCallback(true))
    }


    fun setOutPutPath(outPutPath: String) {
        compressParams.outputPath = outPutPath
    }

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
        compressParams.maxWidth = maxWidth
        compressParams.maxHeight = maxHeight
    }

    /**
     * 设置压缩后保存图片的质量
     *
     * @param quality 图片质量 0 - 100
     */
    fun setQuality(quality: Int) {
        compressParams.saveQuality = quality
    }

    /**
     * set image compress format
     *
     * @param compressFormat compress format
     */
    fun setCompressFormat(compressFormat: Bitmap.CompressFormat) {
        compressParams.compressFormat = compressFormat
    }

    fun onActivityResult(context: Context, requestCode: Int, resultCode: Int, data: Intent) {
        mImagePickHelper.onActivityResult(requestCode, resultCode, data)
        mImageCaptureHelper.onActivityResult(context, requestCode, resultCode, data)
    }

    fun onRequestPermissionsResult(context: Context, requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == mImagePickHelper.requestCode) {
            mImagePickHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
        } else if (requestCode == mImageCaptureHelper.requestCode) {
            mImageCaptureHelper.onRequestPermissionsResult(context, requestCode, permissions, grantResults)
        } else {
            permissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
        callWithCheckPermission(activity) {
            mImagePickHelper.selectorImage(activity, requestCode)
        }
    }

    fun selectImage(fragment: Fragment, requestCode: Int) {
        callWithCheckPermission(fragment) {
            mImagePickHelper.selectImage(fragment, requestCode)
        }
    }

    fun takePhoto(activity: Activity, requestCode: Int) {
        callWithCheckPermission(activity) {
            mImageCaptureHelper.captureImage(activity, requestCode)
        }
    }

    fun takePhoto(fragment: Fragment, requestCode: Int) {
        callWithCheckPermission(fragment) {
            mImageCaptureHelper.captureImage(fragment, requestCode)
        }
    }

    private fun callWithCheckPermission(activity: Activity, callback: () -> Unit) {
        if (needRequestPermissions(activity)) {
            permissionsHelper.checkAndRequestPermission(activity, REQUEST_PERMISSION_CODE, {
                if (it) {
                    callback.invoke()
                } else {
                    mCallback?.onError(ErrorResult.permissionDenied)
                }
            }, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            callback.invoke()
        }
    }

    private fun callWithCheckPermission(fragment: Fragment, callback: () -> Unit) {
        val context = fragment.context

        if (context == null) {
            mCallback?.onError(ErrorResult.error)
            return
        }

        if (needRequestPermissions(context)) {
            permissionsHelper.checkAndRequestPermission(fragment, REQUEST_PERMISSION_CODE, {
                if (it) {
                    callback.invoke()
                } else {
                    mCallback?.onError(ErrorResult.permissionDenied)
                }
            }, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            callback.invoke()
        }
    }

    private fun needRequestPermissions(context: Context): Boolean {

        compressParams.outputPath?.let {
            val outDir = File(it)

            if (outDir.absolutePath.startsWith(context.externalCacheDir.absolutePath)) {
                return false
            }

            if (outDir.absolutePath.startsWith(context.filesDir.absolutePath)) {
                return false
            }
            return true
        }
        return false
    }


    private inner class ImageGetCallback constructor(private val mDeleteOutputFile: Boolean) : Callback {


        override fun onError(errorResult: ErrorResult) {
            mCallback?.onError(errorResult)

        }

        override fun onSuccess(file: String) {

            AppLogger.d(TAG, "get file: $file")

            val jop = ImageCompressHelper.CompressJop()
            jop.params = compressParams
            jop.inputFile = file
            jop.deleteInputFile = mDeleteOutputFile
            mImageCompressHelper.compress(jop)
        }
    }

    interface Callback {
        fun onError(errorResult: ErrorResult)

        fun onSuccess(file: String)
    }

    companion object {

        private const val TAG = "ImageFileSelector"

        private const val REQUEST_PERMISSION_CODE = 372


        fun setDebug(debug: Boolean) {
            AppLogger.DEBUG = debug
        }
    }
}
