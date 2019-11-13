package com.sw926.imagefileselector


import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import androidx.core.content.FileProvider
import android.text.TextUtils
import com.sw926.imagefileselector.ErrorResult.*
import java.io.File


class ImageCaptureHelper {

    private var mCallback: ImageFileSelector.Callback? = null

    private val mPermissionsHelper = PermissionsHelper()
    private val mPermissionCallback: (Boolean) -> Unit = { isGranted: Boolean ->
        if (isGranted) {
            capture()
        } else {
            mCallback?.onError(permissionDenied)
        }
    }

    private var mOutputFile: File? = null

    private var mFragment: Fragment? = null
    private var mActivity: Activity? = null
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
                val outputFilePath = outState.getString("output_file")
                if (!TextUtils.isEmpty(outputFilePath)) {
                    mOutputFile = File(outputFilePath)
                }
            }
        }
    }

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

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        mPermissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    internal fun getRealPathFromUri(context: Context, contentUri: Uri): String? {
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

    private fun createIntent(context: Context): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        mOutputFile?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val cameraTempUri = FileProvider.getUriForFile(context, context.packageName + ".com.sw926.imagefileselector.provider", it)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraTempUri)
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            } else {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mOutputFile))
            }
        }
        return intent
    }

    fun captureImage(activity: Activity, requestCode: Int) {
        this.requestCode = requestCode
        this.mActivity = activity
        this.mFragment = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            mPermissionsHelper.checkAndRequestPermission(activity, requestCode, mPermissionCallback, *getRequestPermissions(activity))
        } else {
            capture()
        }
    }

    fun captureImage(fragment: Fragment, requestCode: Int) {
        this.requestCode = requestCode
        this.mActivity = null
        this.mFragment = fragment

        val context = fragment.context
        if (context == null) {
            mCallback?.onError(error)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPermissionsHelper.checkAndRequestPermission(fragment, requestCode, mPermissionCallback, *getRequestPermissions(context))
        } else {
            capture()
        }
    }

    private fun capture() {
        if (!CommonUtils.hasSDCardMounted()) {
            mCallback?.onError(ErrorResult.error)
            return
        }


        try {
            AppLogger.i(TAG, "start capture image")


            (mActivity ?: mFragment?.context)?.let { context ->

                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (cameraIntent.resolveActivity(context.packageManager) == null) {
                    mCallback?.onError(error)
                    return
                }

                val values = ContentValues(1)
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")

                val fileName = "img_" + System.currentTimeMillis() + ".jpg"
                mOutputFile = File(context.externalCacheDir, fileName)


                if (mOutputFile?.parentFile?.exists() != true) {
                    mOutputFile?.absoluteFile?.mkdirs()
                }
                AppLogger.d(TAG, "capture ouput file: $mOutputFile")

                if (mActivity != null) {
                    mActivity?.startActivityForResult(createIntent(context), requestCode)
                    return
                }

                if (mFragment != null) {
                    mFragment?.startActivityForResult(createIntent(context), requestCode)
                }
            }


        } catch (e: Throwable) {
            mCallback?.onError(ErrorResult.error)
            AppLogger.printStackTrace(e)
        }

    }

    companion object {

        private const val TAG = "ImageCaptureHelper"

        private fun getRequestPermissions(context: Context): Array<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (hasPermissionInManifest(context, Manifest.permission.CAMERA)) {
                    arrayOf(/*Manifest.permission.READ_EXTERNAL_STORAGE,*/ Manifest.permission.CAMERA)
                } else {
                    arrayOf()/*Manifest.permission.READ_EXTERNAL_STORAGE*/
                }
            } else {
                arrayOf()
            }
        }

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
}
