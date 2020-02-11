package com.sw926.imagefileselector


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import com.sw926.imagefileselector.ErrorResult.error
import com.sw926.imagefileselector.ErrorResult.permissionDenied
import java.io.File

class ImagePickHelper(private val mContext: Context) {

    companion object {
        private const val TAG = "ImagePickHelper"
    }

    private var mCallback: ImageFileSelector.Callback? = null
    private var mFragment: Fragment? = null
    private var mActivity: Activity? = null
    var requestCode = -1
        private set

    private var mType = "image/*"

    private val mPermissionsHelper = PermissionsHelper()

    private val mPermissionCallback = { isGranted: Boolean ->
        if (isGranted) {
            startSelect()
        } else {
            mCallback?.onError(permissionDenied)
        }
    }

    fun setType(type: String) {
        mType = type
    }

    fun setCallback(callback: ImageFileSelector.Callback) {
        mCallback = callback
    }

    fun selectImage(fragment: Fragment, requestCode: Int) {
        AppLogger.i(TAG, "start select image from fragment")
        this.requestCode = requestCode
        this.mFragment = fragment
        this.mActivity = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mPermissionsHelper.checkAndRequestPermission(fragment, this.requestCode, mPermissionCallback, Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            startSelect()
        }
    }

    fun selectorImage(activity: Activity, requestCode: Int) {
        AppLogger.i(TAG, "start select image from activity")
        this.requestCode = requestCode
        this.mActivity = activity
        this.mFragment = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mPermissionsHelper.checkAndRequestPermission(activity, this.requestCode, mPermissionCallback, Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            startSelect()
        }
    }

    private fun startSelect() {
        AppLogger.i(TAG, "start system gallery activity")
        if (mActivity != null) {
            try {
                mActivity?.startActivityForResult(createIntent(), requestCode)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return
        }

        if (mFragment != null) {
            try {
                mFragment?.startActivityForResult(createIntent(), requestCode)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return
        }
        AppLogger.e(TAG, "activity or fragment is null")
        mCallback?.onError(error)
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

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == this.requestCode) {
            if (resultCode == Activity.RESULT_CANCELED) {
                AppLogger.i(TAG, "canceled select image")
                mCallback?.onError(ErrorResult.canceled)
            } else if (resultCode == Activity.RESULT_OK) {
                if (intent == null) {
                    AppLogger.e(TAG, "select image error, intent null")
                    mCallback?.onError(error)
                } else {
                    val uri = intent.data
                    val path = Compatibility.getPath(mContext, uri)
                    if (!TextUtils.isEmpty(path) && File(path).exists()) {
                        AppLogger.i(TAG, "select image success: $path")
                        mCallback?.onSuccess(path)
                    } else {
                        AppLogger.e(TAG, "select image file path $path is error or not exists")
                        mCallback?.onError(error)
                    }
                }

            }
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        mPermissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}
