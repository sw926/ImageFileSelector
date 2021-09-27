package com.sw926.imagefileselector

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference

class ImageCaptureHelper {

    private val requestPermission: ActivityResultLauncher<String?>
    private val requestTakePicture: ActivityResultLauncher<Uri>
    private val context: WeakReference<Context>

    constructor(activity: AppCompatActivity) {
        requestPermission = activity.registerForActivityResult(RequestPermission()) {
            if (it) {
                requestTakePicture()
            } else {
                listenerUri?.onCancel()
            }
        }
        requestTakePicture = activity.registerForActivityResult(TakePicture()) { onTakePictureResult(it) }
        context = WeakReference(activity)
    }

    constructor(fragment: Fragment) {
        requestPermission = fragment.registerForActivityResult(RequestPermission()) {
            if (it) {
                requestTakePicture()
            } else {
                listenerUri?.onCancel()
            }
        }
        requestTakePicture = fragment.registerForActivityResult(TakePicture()) {
            onTakePictureResult(it)
        }
        context = WeakReference(fragment.context)
    }


    private var listenerUri: ImageUriResultListener? = null
    private var cameraUri: Uri? = null

    fun setListener(listenerUri: ImageUriResultListener) {
        this.listenerUri = listenerUri
    }

    private fun requestPermission() {
        requestPermission.launch(Manifest.permission.CAMERA)
    }

    private fun requestTakePicture() {

        val context = this.context.get() ?: return

        val file = CommonUtils.generateImageCacheFile(context, ".jpg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraUri = CommonUtils.getFileUri(context, file)
            requestTakePicture.launch(cameraUri)
        } else {
            cameraUri = Uri.fromFile(file)
            requestTakePicture.launch(cameraUri)
        }
    }

    private fun onTakePictureResult(result: Boolean) {
        val resultUri = cameraUri
        if (!result || resultUri == null) {
            listenerUri?.onCancel()
            return
        }
        listenerUri?.onSuccess(resultUri)
    }

    fun takePicture() {
        val context = this.context.get() ?: return
        if (needRequestCameraPermission(context)) {
            requestPermission()
        } else {
            requestTakePicture()
        }
    }

    private fun needRequestCameraPermission(context: Context): Boolean {
        val packageName = context.packageName
        try {
            val packageInfo = context.packageManager
                .getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val declaredPermission = packageInfo.requestedPermissions
            if (declaredPermission != null && declaredPermission.isNotEmpty()) {
                for (p in declaredPermission) {
                    if (p == Manifest.permission.CAMERA) {
                        return true
                    }
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return false
    }
}
