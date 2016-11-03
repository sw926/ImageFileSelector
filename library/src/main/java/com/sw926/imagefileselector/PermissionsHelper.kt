package com.sw926.imagefileselector

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat

/**
 * Created by sunwei on 7/1/16.
 */
internal object PermissionsHelper {

    fun checkAndRequestPermission(activity: Activity, requestCode: Int): Boolean {
        if (isHavePermission(activity)) {
            return true
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), requestCode)
            }
            return false
        }
    }

    fun checkAndRequestPermission(fragment: Fragment, requestCode: Int): Boolean {
        if (isHavePermission(fragment.context)) {
            return true
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                fragment.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), requestCode)
            }
            return false
        }
    }

    private fun isHavePermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}
