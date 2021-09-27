package com.sw926.imagefileselector

import android.net.Uri

/**
 *
 * @author: sunwei
 * @date: 2021/9/27
 */
interface ImageUriResultListener {

    fun onSuccess(uri: Uri)

    fun onCancel()

    fun onError()
}