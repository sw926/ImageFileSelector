package com.sw926.imagefileselector


import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import java.util.*

/**
 * Created by sunwei on 7/1/16.
 */
class PermissionsHelper {

    private var mCurrentCallback: ((isGrant: Boolean) -> Unit)? = null
    private var mCurrentRequestPermissions: Array<String>? = null

    private var mCurrentRequestCode: Int = 0

    fun checkAndRequestPermission(activity: Activity, requestCode: Int, callback: (Boolean) -> Unit, vararg permissions: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val unGrantPermissions = getUnGrantPermissions(activity, *permissions)
            if (unGrantPermissions.isEmpty()) {
                callback.invoke(true)
                setCurrentRequest(null, null, -1)
            } else {
                setCurrentRequest(callback, unGrantPermissions, requestCode)
                activity.requestPermissions(unGrantPermissions, requestCode)
            }
        } else {
            callback.invoke(true)
            setCurrentRequest(null, null, -1)
        }
    }

    fun checkAndRequestPermission(fragment: Fragment, requestCode: Int, callback: (Boolean) -> Unit, vararg permissions: String) {
        val context = fragment.context
        if (context == null) {
            mCurrentCallback?.invoke(false)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val unGrantPermissions = getUnGrantPermissions(context, *permissions)
            if (unGrantPermissions.isEmpty()) {
                callback.invoke(true)
                setCurrentRequest(null, null, -1)
            } else {
                setCurrentRequest(callback, unGrantPermissions, requestCode)
                fragment.requestPermissions(unGrantPermissions, requestCode)
            }
        } else {
            callback.invoke(true)
            setCurrentRequest(null, null, -1)
        }
    }

    private fun getUnGrantPermissions(context: Context, vararg permissions: String): Array<String> {

        val unGrantPermissions = ArrayList<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    unGrantPermissions.add(permission)
                }
            }
        }
        return unGrantPermissions.toTypedArray()
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        var isAllGranted = true
        if (requestCode == mCurrentRequestCode) {
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false
                    break
                }
            }
        }
        mCurrentCallback?.invoke(isAllGranted)
    }

    private fun setCurrentRequest(currentCallback: ((Boolean) -> Unit)?, currentRequestPermissions: Array<String>?, requestCode: Int) {
        mCurrentCallback = currentCallback
        mCurrentRequestPermissions = currentRequestPermissions
        mCurrentRequestCode = requestCode
    }
}
