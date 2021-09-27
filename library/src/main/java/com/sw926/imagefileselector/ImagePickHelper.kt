package com.sw926.imagefileselector

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sw926.imagefileselector.contract.PickImageContract

class ImagePickHelper {

    private var listenerUri: ImageUriResultListener? = null
    private val requestPick: ActivityResultLauncher<String?>

    private var type = "image/*"

    constructor(activity: AppCompatActivity) {
        requestPick = activity.registerForActivityResult(PickImageContract()) { onSelectResult(it) }
    }

    constructor(fragment: Fragment) {
        requestPick = fragment.registerForActivityResult(PickImageContract()) { onSelectResult(it) }
    }

    fun setType(type: String) {
        this.type = type
    }

    fun setListener(listenerUri: ImageUriResultListener) {
        this.listenerUri = listenerUri
    }

    fun pickImage() {
        requestPick.launch(type)
    }

    private fun onSelectResult(uri: Uri?) {
        if (uri == null) {
            listenerUri?.onCancel()
        } else {
            listenerUri?.onSuccess(uri)
        }
    }

}
