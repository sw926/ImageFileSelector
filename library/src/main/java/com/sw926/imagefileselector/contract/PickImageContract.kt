package com.sw926.imagefileselector.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore.Images.Media
import androidx.activity.result.contract.ActivityResultContract

/**
 *
 * @author: sunwei
 * @date: 2021/9/26
 */
class PickImageContract : ActivityResultContract<String?, Uri?>() {

    override fun createIntent(context: Context, input: String?): Intent {
        val pickIntent = Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = input ?: "image/*"
        return pickIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
    }
}