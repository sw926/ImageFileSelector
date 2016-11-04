package com.sw926.imagefileselector

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import java.io.File

@SuppressWarnings("unused")
class ImageCropper {


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
    var mRequestCode = -1

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

    fun setCallback(callback: ImageCropperCallback) {
        mCallback = callback
    }

    private val context: Context?
        get() {
            activity?.let {
                return it
            }
            fragment?.let {
                return it.context
            }
            return null
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
                mOutPutX = bundle.getInt("outgetX")
                mOutPutY = bundle.getInt("outgetY")
                mAspectX = bundle.getInt("aspectX")
                mAspectY = bundle.getInt("aspectY")
                mScale = bundle.getBoolean("scale")
                mOutFile = bundle.getSerializable("outFile") as File?
                mSrcFile = bundle.getSerializable("srcFile") as File?
                mTempFile = bundle.getSerializable("tempFile") as File?
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == mRequestCode) {
            if (resultCode == Activity.RESULT_CANCELED) {
                mCallback?.onError(CropperErrorResult.canceled)
                return
            }
            mTempFile?.let { file ->
                file.exists().let {
                    AppLogger.i(TAG, "delete temp file: ${file.path}")
                    file.delete()
                }
            }

            mOutFile?.let {
                if (it.exists()) {
                    AppLogger.i(TAG, "crop success output file: ${it.path}")
                    mCallback?.onSuccess(it.path)
                    return
                }
            }

            context?.let { ctx ->
                data?.data?.let {
                    Compatibility.getPath(ctx, it)?.let {
                        val outputFile = File(it)
                        if (outputFile.exists()) {
                            AppLogger.i(TAG, "crop success output file: $it")
                            mCallback?.onSuccess(it)
                            return
                        }
                    }
                }
            }

            context?.let { ctx ->
                data?.let {
                    val bitmap = it.getParcelableExtra<Bitmap>("data")
                    bitmap?.let {
                        val outputFile = CommonUtils.generateExternalImageCacheFile(ctx, ".jpg")
                        ImageUtils.saveBitmap(bitmap, outputFile.path, CompressFormat.JPEG, 80)
                        AppLogger.i(TAG, "create output file from data: ${outputFile.path}")
                        mCallback?.onSuccess(outputFile.path)
                        return
                    }
                }
            }
            mCallback?.onError(CropperErrorResult.error)
        }
    }

    fun cropImage(activity: Activity, srcFile: String, requestCode: Int) {
        this.fragment = null
        this.activity = activity
        this.mRequestCode = requestCode
        cropImage(srcFile)
    }

    fun cropImage(fragment: Fragment, srcFile: String, requestCode: Int) {
        this.fragment = fragment
        this.activity = activity
        this.mRequestCode = requestCode
        cropImage(srcFile)
    }

    private fun cropImage(srcFile: String) {
        try {


            AppLogger.i(TAG, "------------------ start crop file ---------------")

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

            val contextNotNull = context!!
            val outFile = CommonUtils.generateExternalImageCacheFile(contextNotNull, ".jpg")
            if (!outFile.parentFile.exists()) {
                outFile.parentFile.mkdirs()
            }
            if (outFile.exists()) {
                outFile.delete()
            }
            AppLogger.i(TAG, "set output file: ${outFile.path}")


            mSrcFile = inputFile
            mOutFile = outFile

            val uri: Uri
            if (inputFile.path.contains("%")) {
                val inputFileName = srcFile
                val ext = inputFileName.substring(inputFileName.lastIndexOf("."))
                mTempFile = CommonUtils.generateExternalImageCacheFile(contextNotNull, ext)
                CommonUtils.copy(inputFile, mTempFile!!)
                uri = FileProvider.getUriForFile(context, "com.sw926.fileprovider", mTempFile)
                AppLogger.w(TAG, "use temp file:" + mTempFile!!.path)
            } else {
                uri = FileProvider.getUriForFile(context, "com.sw926.fileprovider", inputFile)
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
                intent.putExtra("scaleUpIfNeeded", true)// 黑边
            }
            if (Compatibility.scaleUpIfNeeded4Black()) {
                intent.putExtra("scaleUpIfNeeded", true)
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outFile))
            if (Compatibility.shouldReturnCropData()) {
                intent.putExtra("return-data", true)
            }

            activity?.let {
                try {
                    it.startActivityForResult(intent, mRequestCode)
                } catch (e: ActivityNotFoundException) {
                    mCallback?.onError(CropperErrorResult.notSupport)
                }
            }

            fragment?.let {
                try {
                    it.startActivityForResult(intent, mRequestCode)
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
        fun onError(result: CropperErrorResult)
        fun onSuccess(outputFile: String)
    }

    companion object {

        private val TAG = ImageCropper::class.java.simpleName

        private val IMAGE_CROPPER_BUNDLE = "IMAGE_CROPPER_BUNDLE"
    }


}
