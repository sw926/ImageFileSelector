package com.sw926.imagefileselector

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG

data class CompressParams(
    val maxWidth: Int = 1000,
    val maxHeight: Int = 1000,
    val saveQuality: Int = 80,
    val compressFormat: Bitmap.CompressFormat = JPEG,
)