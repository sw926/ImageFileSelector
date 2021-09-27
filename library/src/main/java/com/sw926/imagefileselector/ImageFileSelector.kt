@file:JvmName("ImageFileSelector")

package com.sw926.imagefileselector

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference

class ImageFileSelector {

    private var resultListener: ImageFileResultListener? = null
    private val mImagePickHelper: ImagePickHelper
    private val mImageCaptureHelper: ImageCaptureHelper

    private var compressParams: CompressParams
    private val context: WeakReference<Context>

    constructor(activity: AppCompatActivity) {
        mImageCaptureHelper = ImageCaptureHelper(activity)
        mImagePickHelper = ImagePickHelper(activity)

        compressParams = CompressParams()
        context = WeakReference(activity)

        init()
    }

    constructor(fragment: Fragment) {
        mImageCaptureHelper = ImageCaptureHelper(fragment)
        mImagePickHelper = ImagePickHelper(fragment)

        compressParams = CompressParams()
        context = WeakReference(fragment.context)
        init()
    }

    private fun init() {
        val listener = object : ImageUriResultListener {
            override fun onSuccess(uri: Uri) {

                val context = this@ImageFileSelector.context.get()
                if (context == null) {
                    resultListener?.onError()
                    return
                }
                ImageUtils.compress(context, uri, compressParams) {
                    if (it != null) {
                        resultListener?.onSuccess(it)
                    } else {
                        resultListener?.onError()
                    }
                }
            }

            override fun onCancel() {
            }

            override fun onError() {
            }
        }

        mImagePickHelper.setListener(listener)
        mImageCaptureHelper.setListener(listener)
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


    fun setListener(listenerUri: ImageFileResultListener?) {
        resultListener = listenerUri
    }

    fun selectImage() {
        mImagePickHelper.pickImage()
    }

    fun takePhoto() {
        mImageCaptureHelper.takePicture()
    }

}
