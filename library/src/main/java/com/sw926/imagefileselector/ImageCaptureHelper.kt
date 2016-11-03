package com.sw926.imagefileselector

import android.annotation.TargetApi
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.app.Fragment
import java.io.File

class ImageCaptureHelper {

    var errorCallback: ((ErrorResult) -> Unit)? = null
    var successCallback: ((String) -> Unit)? = null

    private var fragment: Fragment? = null
    private var activity: Activity? = null
    private var mCameraTempUri: Uri? = null
    var mRequestCode: Int = 0

    private fun getContext(): Context? {
        activity?.let {
            return it
        }

        fragment?.let {
            return it.activity
        }

        return null
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        getContext()?.let { context ->
            if (requestCode == mRequestCode) {
                if (resultCode == Activity.RESULT_CANCELED) {
                    errorCallback?.invoke(ErrorResult.canceled)
                } else if (resultCode == Activity.RESULT_OK) {
                    mCameraTempUri?.let {
                        val file = File(getRealPathFromUri(context, it))
                        if (file.exists()) {
                            successCallback?.invoke(file.path)
                        } else {
                            errorCallback?.invoke(ErrorResult.error)
                        }
                    }
                }
            }
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == mRequestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                capture()
            } else {
                errorCallback?.invoke(ErrorResult.permissionDenied)
            }
        }
    }

    fun getRealPathFromUri(context: Context, contentUri: Uri): String? {
        var cursor: Cursor? = null
        try {
            val param = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, param, null, null, null)
            if (cursor != null) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                return cursor.getString(columnIndex)
            }
        } catch (e: Exception) {
            AppLogger.printStackTrace(e)
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }
        return null
    }

    private fun createIntent(): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        if (mCameraTempUri != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraTempUri)
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
        }
        return intent
    }

    @Suppress("unused")
    fun captureImage(activity: Activity, requestCode: Int) {
        this.mRequestCode = requestCode
        this.activity = activity
        this.fragment = null

        this.activity?.let {
            if (PermissionsHelper.checkAndRequestPermission(it, mRequestCode)) {
                capture()
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Suppress("unused")
    fun captureImage(fragment: Fragment, requestCode: Int) {
        this.mRequestCode = requestCode
        this.activity = null
        this.fragment = fragment

        this.fragment?.let {
            if (PermissionsHelper.checkAndRequestPermission(it, mRequestCode)) {
                capture()
            }
        }
    }

    private fun capture() {
        try {
            getContext()?.let {
                val values = ContentValues(1)
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
                mCameraTempUri = it.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }

            activity?.let {
                it.startActivityForResult(createIntent(), mRequestCode)
                return
            }

            fragment?.let {
                it.startActivityForResult(createIntent(), mRequestCode)
                return
            }

        } catch (e: ActivityNotFoundException) {
            AppLogger.printStackTrace(e)
        }
    }
}
