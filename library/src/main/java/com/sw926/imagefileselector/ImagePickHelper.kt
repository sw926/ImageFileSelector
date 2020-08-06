package com.sw926.imagefileselector


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.sw926.imagefileselector.ErrorResult.error
import com.sw926.imagefileselector.ErrorResult.permissionDenied
import java.lang.ref.WeakReference

class ImagePickHelper {

    companion object {
        private const val TAG = "ImagePickHelper"
    }

    private var callerWeakReference: WeakReference<Any>? = null

    private var mCallback: ImageFileSelector.Callback? = null

    var requestCode = -1
        private set

    private var mType = "image/*"

    fun setType(type: String) {
        mType = type
    }

    fun setCallback(callback: ImageFileSelector.Callback) {
        mCallback = callback
    }

    fun selectImage(fragment: Fragment, requestCode: Int) {
        AppLogger.i(TAG, "start select image from fragment")
        this.requestCode = requestCode
        callerWeakReference = WeakReference(fragment)

        val context = fragment.context
        if (context == null) {
            mCallback?.onError(error)
            return
        }

        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (PermissionsHelper.isGrant(context, *permissions)) {
            startSelect()
        } else {
            fragment.requestPermissions(permissions, requestCode)
        }
    }

    fun selectorImage(activity: Activity, requestCode: Int) {
        AppLogger.i(TAG, "start select image from activity")
        this.requestCode = requestCode
        callerWeakReference = WeakReference(activity)

        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (PermissionsHelper.isGrant(activity, *permissions)) {
            startSelect()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(permissions, requestCode)
            }
        }
    }

    private fun startSelect() {
        AppLogger.i(TAG, "start system gallery activity")
        when (val caller = callerWeakReference?.get()) {
            is Fragment -> {
                try {
                    caller.startActivityForResult(createIntent(), requestCode)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            is Activity -> {
                try {
                    caller.startActivityForResult(createIntent(), requestCode)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else -> {
                AppLogger.e(TAG, "activity or fragment is null")
                mCallback?.onError(error)
            }
        }
    }

    private fun createIntent(): Intent {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent(Intent.ACTION_OPEN_DOCUMENT)
        } else {
            Intent(Intent.ACTION_GET_CONTENT)
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P
                && Build.BOARD == "Xiaomi"
                && Build.MODEL == "MIX 2") {
            intent.type = "*/*"

        } else {
            intent.type = mType
        }
        return intent
    }


    fun onSaveInstanceState(outState: Bundle) {
        if (requestCode > 0) {
            outState.putInt("image_pick_request_code", requestCode)
        }
    }

    fun onRestoreInstanceState(outState: Bundle?) {
        if (outState != null && outState.containsKey("image_pick_request_code")) {
            requestCode = outState.getInt("image_pick_request_code")
        }
    }

    private fun getContext(): Context? {
        val caller = callerWeakReference?.get()
        if (caller is Fragment) {
            return caller.context
        } else if (caller is Activity) {
            return caller
        }
        return null
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == this.requestCode) {
            if (resultCode == Activity.RESULT_CANCELED) {
                AppLogger.i(TAG, "canceled select image")
                mCallback?.onError(ErrorResult.canceled)
            } else if (resultCode == Activity.RESULT_OK) {
                val context = getContext()
                if (context == null) {
                    mCallback?.onError(error)
                    return
                }
                if (intent == null) {
                    AppLogger.e(TAG, "select image error, intent null")
                    mCallback?.onError(error)
                    return
                }
                intent.data?.let { Compatibility.getPath(context, it) }?.let { path ->
                    if (path.isNotBlank()) {
                        mCallback?.onSuccess(path)
                        return
                    }
                }
                mCallback?.onError(error)
            }
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (PermissionsHelper.isAllGrant(grantResults)) {
            startSelect()
        } else {
            mCallback?.onError(permissionDenied)
        }
    }

}
