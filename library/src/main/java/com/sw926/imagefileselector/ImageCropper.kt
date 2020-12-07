package com.sw926.imagefileselector

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.sw926.imagefileselector.CommonUtils.generateImageCacheFile
import com.sw926.imagefileselector.ImageUtils.saveBitmap
import java.io.File

class ImageCropper {
    companion object {
        private val TAG = ImageCropper::class.java.simpleName
        private const val IMAGE_CROPPER_BUNDLE = "IMAGE_CROPPER_BUNDLE"
    }

    enum class CropperErrorResult {
        error,
        canceled,
        notSupport
    }

    private var mOutPutX = -1
    private var mOutPutY = -1
    private var mAspectX = -1
    private var mAspectY = -1
    private var mScale = true
    private var mSrcFile: File? = null
    private var mOutFile: File? = null
    private var mRequestCode = -1

    /**
     * 记录裁切过程中产生的临时文件，裁切完成后进行删除
     */
    private var mTempFile: File? = null
    private var mCallback: ImageCropperCallback? = null
    private var fragment: Fragment? = null
    private var activity: Activity? = null
    fun setOutPut(width: Int, height: Int) {
        mOutPutX = width
        mOutPutY = height
    }

    fun setOutPutAspect(width: Int, height: Int) {
        mAspectX = width
        mAspectY = height
    }

    fun setScale(scale: Boolean) {
        mScale = scale
    }

    fun setCallback(callback: ImageCropperCallback?) {
        mCallback = callback
    }

    private val context: Context?
        private get() {
            if (activity != null) {
                return activity
            }
            return fragment?.context
        }

    fun onSaveInstanceState(outState: Bundle) {
        val bundle = Bundle()
        bundle.putInt("outputX", mOutPutX)
        bundle.putInt("outputY", mOutPutY)
        bundle.putInt("aspectX", mAspectX)
        bundle.putInt("aspectY", mAspectY)
        bundle.putBoolean("scale", mScale)
        bundle.putSerializable("outFile", mOutFile)
        bundle.putSerializable("srcFile", mSrcFile)
        bundle.putSerializable("tempFile", mTempFile)
        outState.putBundle(IMAGE_CROPPER_BUNDLE, bundle)
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null && savedInstanceState.containsKey(IMAGE_CROPPER_BUNDLE)) {
            val bundle = savedInstanceState.getBundle(IMAGE_CROPPER_BUNDLE)
            if (bundle != null) {
                mOutPutX = bundle.getInt("outputX")
                mOutPutY = bundle.getInt("outputY")
                mAspectX = bundle.getInt("aspectX")
                mAspectY = bundle.getInt("aspectY")
                mScale = bundle.getBoolean("scale")
                mOutFile = bundle.getSerializable("outFile") as File?
                mSrcFile = bundle.getSerializable("srcFile") as File?
                mTempFile = bundle.getSerializable("tempFile") as File?
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (mRequestCode > 0 && requestCode == mRequestCode) {

            val context = this.context
            if (context == null) {
                mCallback?.onError(CropperErrorResult.error)
                return
            }

            if (resultCode == Activity.RESULT_CANCELED) {
                mCallback?.onError(CropperErrorResult.canceled)
                return
            }
            if (mTempFile?.exists() == true) {
                AppLogger.i(TAG, "delete temp file: " + mTempFile?.path)
                mTempFile?.delete()
            }
            if (mOutFile?.exists() == true) {
                AppLogger.i(TAG, "crop success output file: " + mOutFile?.path)
                mOutFile?.path?.let { mCallback?.onSuccess(it) }
                return
            }


            intent?.data?.let { Compatibility.getPath(context, it) }?.let { path ->
                if (path.isNotBlank()) {
                    val outputFile = File(path)
                    if (outputFile.exists()) {
                        AppLogger.i(TAG, "crop success output file:$path")
                        mCallback?.onSuccess(path)
                        return
                    }
                }
            }

            val bitmap = intent?.getParcelableExtra<Bitmap>("data")
            if (bitmap != null) {
                val outputFile = generateImageCacheFile(context, ".jpg")
                saveBitmap(bitmap, outputFile.path, Bitmap.CompressFormat.JPEG, 80)
                AppLogger.i(TAG, "create output file from data: " + outputFile.path)

                mCallback?.onSuccess(outputFile.path)
                return
            }

            mCallback?.onError(CropperErrorResult.error)
        }
    }

    fun cropImage(activity: Activity?, srcFile: String, requestCode: Int) {
        fragment = null
        this.activity = activity
        mRequestCode = requestCode
        cropImage(srcFile)
    }

    fun cropImage(fragment: Fragment?, srcFile: String, requestCode: Int) {
        this.fragment = fragment
        activity = null
        mRequestCode = requestCode
        cropImage(srcFile)
    }

    private fun cropImage(srcFile: String) {
        try {
            AppLogger.i(TAG, "------------------ start crop file ---------------")
            val context = context
            if (context == null) {
                AppLogger.e(TAG, "fragment or activity is null")
                mCallback?.onError(CropperErrorResult.error)
                return
            }
            val inputFile = File(srcFile)
            if (!inputFile.exists()) {
                AppLogger.e(TAG, "input file not exists")
                mCallback?.onError(CropperErrorResult.error)
                return
            }
            val outFile = generateImageCacheFile(context, ".jpg")
            if (!outFile.parentFile.exists()) {
                outFile.parentFile.mkdirs()
            }
            if (outFile.exists()) {
                outFile.delete()
            }
            AppLogger.i(TAG, "set output file: " + outFile.path)
            mSrcFile = inputFile
            mOutFile = outFile
            val uri: Uri
            if (inputFile.path.contains("%")) {
                val ext = srcFile.substring(srcFile.lastIndexOf("."))
                val tFile = generateImageCacheFile(context, ext)
                inputFile.copyTo(tFile, true, 1024)
                mTempFile = tFile
                uri = CommonUtils.getFileUri(context, tFile)
            } else {
                uri = CommonUtils.getFileUri(context, inputFile)
            }
            val intent = Intent("com.android.camera.action.CROP")
            intent.setDataAndType(uri, "image/*")
            intent.putExtra("crop", "true")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (mAspectX > 0 && mAspectY > 0) {
                intent.putExtra("aspectX", mAspectX)
                intent.putExtra("aspectY", mAspectY)
            }
            if (mOutPutX > 0 && mOutPutY > 0) {
                intent.putExtra("outputX", mOutPutX)
                intent.putExtra("outputY", mOutPutY)
            }
            if (mScale) {
                intent.putExtra("scale", true)
                intent.putExtra("scaleUpIfNeeded", true) // 黑边
            }
            if (Compatibility.scaleUpIfNeeded4Black()) {
                intent.putExtra("scaleUpIfNeeded", true)
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outFile))
            if (Compatibility.shouldReturnCropData()) {
                intent.putExtra("return-data", true)
            }
            if (activity != null) {
                try {
                    activity?.startActivityForResult(intent, mRequestCode)
                } catch (e: ActivityNotFoundException) {
                    mCallback?.onError(CropperErrorResult.notSupport)
                }
            }
            if (fragment != null) {
                try {
                    fragment?.startActivityForResult(intent, mRequestCode)
                } catch (e: ActivityNotFoundException) {
                    mCallback?.onError(CropperErrorResult.notSupport)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mCallback?.onError(CropperErrorResult.error)
        }
    }

    interface ImageCropperCallback {
        fun onError(result: CropperErrorResult?)
        fun onSuccess(outputFile: String)
    }
}