package com.sw926.imagefileselector

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sw926.imagefileselector.contract.CropImageContract
import java.io.File
import java.lang.ref.WeakReference


class ImageCropHelper {

    data class CropParam(
        val inputUri: Uri,
        val outputUri: Uri,
        val outPutX: Int = -1,
        val outPutY: Int = -1,
        val aspectX: Int = -1,
        val aspectY: Int = -1,
        val scale: Boolean = true,
    )

    private val cropImage: ActivityResultLauncher<CropParam>

    private var outPutX = -1
    private var outPutY = -1
    private var aspectX = -1
    private var aspectY = -1
    private var scale = true

    private var outputFile: File? = null

    /**
     * 记录裁切过程中产生的临时文件，裁切完成后进行删除
     */
    private val tempFiles = mutableListOf<String>()
    private var listener: ImageFileResultListener? = null
    private val context: WeakReference<Context>


    constructor(activity: AppCompatActivity) {
        cropImage = activity.registerForActivityResult(CropImageContract()) {
            onCropResult(it)
        }
        context = WeakReference(activity)

    }

    constructor(fragment: Fragment) {
        cropImage = fragment.registerForActivityResult(CropImageContract()) {
            onCropResult(it)
        }
        context = WeakReference(fragment.context)

    }

    private fun onCropResult(uri: Uri?) {
        outputFile?.let {
            if (it.exists()) {
                listener?.onSuccess(it.path)
                return
            }
        }

        if (uri == null) {
            listener?.onCancel()
            return
        }

        val context = this.context.get()
        if (context == null) {
            listener?.onError()
            return
        }

        val filePath = CommonUtils.getPathFromFileProviderUri(context, uri)

        if (filePath != null) {
            listener?.onSuccess(filePath)
        } else {
            listener?.onError()
        }
    }

    fun setOutPut(width: Int, height: Int) {
        outPutX = width
        outPutY = height
    }

    fun setOutPutAspect(width: Int, height: Int) {
        aspectX = width
        aspectY = height
    }

    fun setScale(scale: Boolean) {
        this.scale = scale
    }

    fun setListener(listener: ImageFileResultListener?) {
        this.listener = listener
    }

    private fun getCropperPackageName(context: Context): String? {
        return kotlin.runCatching {
            val intent = Intent("com.android.camera.action.CROP")
            intent.putExtra("crop", "true")
            intent.type = "image/*"
            context.packageManager.resolveActivity(intent, 0)?.activityInfo?.packageName
        }.getOrNull()


    }

    fun cropImage(srcFile: String) {
        val context = this.context.get()
        if (context == null) {
            listener?.onError()
            return
        }

        val cropperPackageName = getCropperPackageName(context)
        if (cropperPackageName == null) {
            listener?.onError()
            return
        }

        val inputFile = CommonUtils.generateImageCacheFile(context, ".jpg")
        File(srcFile).copyTo(inputFile)
        tempFiles.add(inputFile.path)

        val inputUri = CommonUtils.getFileUri(context, inputFile)

        context.grantUriPermission(cropperPackageName, inputUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val outputFile = CommonUtils.generateImageCacheFile(context, ".jpg")
        this.outputFile = outputFile
        val outputUri = CommonUtils.getFileUri(context, outputFile)
        context.grantUriPermission(cropperPackageName, outputUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val param = CropParam(
            inputUri = inputUri,
            outputUri = outputUri,
            outPutX = outPutX,
            outPutY = outPutY,
            aspectX = aspectX,
            aspectY = aspectY,
            scale = scale
        )
        cropImage.launch(param)
    }

}