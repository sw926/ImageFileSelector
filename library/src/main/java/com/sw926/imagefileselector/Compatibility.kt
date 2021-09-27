package com.sw926.imagefileselector

import android.os.Build

internal object Compatibility {
    fun shouldReturnCropData(): Boolean {
        return (Build.DEVICE.contains("milestone2", true)
            || Build.DEVICE.contains("milestone3", true)
            || Build.BOARD.contains("sholes", true)
            || Build.PRODUCT.contains("sholes", true)
            || Build.DEVICE.equals("olympus", true)
            || Build.DEVICE.contains("umts_jordan", true))
    }

    fun scaleUpIfNeeded4Black(): Boolean {
        return Build.DEVICE.contains("mx2", true)
    }

}