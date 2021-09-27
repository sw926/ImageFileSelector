package com.sw926.imagefileselector.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import com.sw926.imagefileselector.Compatibility
import com.sw926.imagefileselector.ImageCropHelper.CropParam

/**
 *
 * @author: sunwei
 * @date: 2021/9/27
 */
class CropImageContract : ActivityResultContract<CropParam, Uri?>() {

    override fun createIntent(context: Context, input: CropParam): Intent {

        val uri = input.inputUri
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")
        intent.putExtra("crop", "true")
        if (input.aspectX > 0 && input.aspectY > 0) {
            intent.putExtra("aspectX", input.aspectX)
            intent.putExtra("aspectY", input.aspectY)
        }
        if (input.outPutX > 0 && input.outPutY > 0) {
            intent.putExtra("outputX", input.outPutX)
            intent.putExtra("outputY", input.outPutY)
        }
        if (input.scale) {
            intent.putExtra("scale", true)
            intent.putExtra("scaleUpIfNeeded", true) // 黑边
        }
        if (Compatibility.scaleUpIfNeeded4Black()) {
            intent.putExtra("scaleUpIfNeeded", true)
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, input.outputUri)
        if (Compatibility.shouldReturnCropData()) {
            intent.putExtra("return-data", true)
        }

        return intent

    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
    }

}