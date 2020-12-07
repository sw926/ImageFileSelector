package com.sw926.imagefileselector


import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.sw926.imagefileselector.ErrorResult.*
import java.io.File
import java.lang.ref.WeakReference


class ImageCaptureHelper {

    companion object {

        private const val TAG = "ImageCaptureHelper"

        private fun getRequestPermissions(context: Context): Array<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (hasPermissionInManifest(context, Manifest.permission.CAMERA)) {
                    arrayOf(Manifest.permission.CAMERA)
                } else {
                    arrayOf()
                }
            } else {
                arrayOf()
            }
        }

        @Suppress("SameParameterValue")
        private fun hasPermissionInManifest(context: Context, permissionName: String): Boolean {
            val packageName = context.packageName
            try {
                val packageInfo = context.packageManager
                        .getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                val declaredPermission = packageInfo.requestedPermissions
                if (declaredPermission != null && declaredPermission.isNotEmpty()) {
                    for (p in declaredPermission) {
                        if (p == permissionName) {
                            return true
                        }
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                AppLogger.printStackTrace(e)
            }

            return false
        }
    }

    private var mCallback: ImageFileSelector.Callback? = null

    private var mOutputFile: File? = null

    private var callerWeakReference: WeakReference<Any>? = null

    var requestCode = -1
        private set

    fun setCallback(callback: ImageFileSelector.Callback?) {
        mCallback = callback
    }

    fun onSaveInstanceState(outState: Bundle?) {
        if (outState != null) {
            if (requestCode > 0) {
                outState.putInt("image_capture_request_code", requestCode)
            }
            mOutputFile?.path.let { outState.putString("output_file", it) }
        }
    }

    fun onRestoreInstanceState(outState: Bundle?) {
        if (outState != null) {
            if (outState.containsKey("image_capture_request_code")) {
                requestCode = outState.getInt("image_capture_request_code")
            }
            if (outState.containsKey("output_file")) {
                val outputFilePath: String? = outState.getString("output_file")
                if (outputFilePath?.isNotBlank() == true) {
                    mOutputFile = File(outputFilePath)
                }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onActivityResult(context: Context?, requestCode: Int, resultCode: Int, intent: Intent?) {
        if (context != null)
            if (requestCode == this.requestCode) {
                if (resultCode == Activity.RESULT_CANCELED) {
                    AppLogger.i(TAG, "canceled capture image")
                    mCallback?.onError(canceled)
                } else if (resultCode == Activity.RESULT_OK) {

                    mOutputFile?.let {
                        if (it.exists()) {
                            AppLogger.i(TAG, "capture image success: " + it.path)
                            mCallback?.onSuccess(it.path)
                        } else {
                            AppLogger.i(TAG, "capture image error " + it.path)
                            mCallback?.onError(error)
                        }
                    }
                }
            }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (PermissionsHelper.isAllGrant(grantResults)) {
            capture()
        } else {
            mCallback?.onError(permissionDenied)
        }
    }

    private fun createIntent(context: Context): Intent {
        val file = File(context.getExternalFilesDir("app_share"), "capture.jpg")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cameraTempUri = FileProvider.getUriForFile(context, "com.example.myapplication", file)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraTempUri)
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mOutputFile))
        }
        return intent
    }

    fun captureImage(activity: Activity, requestCode: Int) {
        this.requestCode = requestCode
        callerWeakReference = WeakReference(activity)
        if (PermissionsHelper.isGrant(activity, *getRequestPermissions(activity))) {
            capture()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(getRequestPermissions(activity), requestCode)
            }
        }
    }

    fun captureImage(fragment: Fragment, requestCode: Int) {
        this.requestCode = requestCode
        callerWeakReference = WeakReference(fragment)
        val context = fragment.context
        if (context == null) {
            mCallback?.onError(error)
            return
        }

        if (PermissionsHelper.isGrant(context, *getRequestPermissions(context))) {
            capture()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                fragment.requestPermissions(getRequestPermissions(context), requestCode)
            }
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

    private fun capture() {
        if (!CommonUtils.hasSDCardMounted()) {
            mCallback?.onError(error)
            return
        }
        try {
            AppLogger.i(TAG, "start capture image")

            val context = getContext()
            if (context == null) {
                mCallback?.onError(error)
                return
            }

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (cameraIntent.resolveActivity(context.packageManager) == null) {
                mCallback?.onError(error)
                return
            }

            val values = ContentValues(1)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")

            val fileName = "img_" + System.currentTimeMillis() + ".jpg"
            mOutputFile = File(CommonUtils.getOutPutPath(context), fileName)


            if (mOutputFile?.parentFile?.exists() != true) {
                mOutputFile?.absoluteFile?.mkdirs()
            }
            AppLogger.d(TAG, "capture ouput file: $mOutputFile")
            when (val caller = callerWeakReference?.get()) {
                is Activity -> {
                    caller.startActivityForResult(createIntent(context), requestCode)
                }
                is Fragment -> {
                    caller.startActivityForResult(createIntent(context), requestCode)
                }
                else -> {
                    mCallback?.onError(error)
                }
            }

        } catch (e: Throwable) {
            mCallback?.onError(error)
            AppLogger.printStackTrace(e)
        }
    }

}
