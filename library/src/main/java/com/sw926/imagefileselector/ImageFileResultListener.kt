package com.sw926.imagefileselector

/**
 *
 * @author: sunwei
 * @date: 2021/9/27
 */
interface ImageFileResultListener {

    fun onSuccess(filePath: String)

    fun onCancel()

    fun onError()
}