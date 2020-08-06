package com.sw926.imagefileselector

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Created by sunwei on 7/1/16.
 */

object PermissionsHelper {

    fun isAllGrant(grantResults: IntArray): Boolean {
        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun isGrant(context: Context, vararg permissions: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        permissions.forEach {
            if (ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

}
