package com.sw926.imagefileselector

import android.content.Context
import android.os.Environment
import java.io.*

object CommonUtils {

    /**
     * copy file

     * @param source source file
     * *
     * @param dest   dest file
     * *
     * @return true if success copied
     */
    fun copy(source: File, dest: File): Boolean {
        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null
        var result = true
        try {
            bis = BufferedInputStream(FileInputStream(source))
            bos = BufferedOutputStream(FileOutputStream(dest, false))

            val buf = ByteArray(1024)
            bis.read(buf)

            do {
                bos.write(buf)
            } while (bis.read(buf) != -1)
        } catch (e: IOException) {
            result = false
        } finally {
            try {
                if (bis != null) bis.close()
                if (bos != null) bos.close()
            } catch (e: IOException) {
                result = false
            }

        }

        return result
    }

    /**
     * 在图片缓存文件夹生成一个临时文件

     * @param context context
     * *
     * @param ext     文件后缀名 e.g ".jpg"
     * *
     * @return 生成的临时文件
     */
    fun generateExternalImageCacheFile(context: Context, ext: String): File {
        val fileName = "img_" + System.currentTimeMillis()
        return generateExternalImageCacheFile(context, fileName, ext)
    }

    private fun generateExternalImageCacheFile(context: Context, fileName: String, ext: String): File {
        val cacheDir = getExternalImageCacheDir(context)
        val path = cacheDir.path + File.separator + fileName + ext
        return File(path)
    }

    fun getExternalImageCacheDir(context: Context): File {
        val externalCacheDir = getExternalCacheDir(context)
        if (externalCacheDir != null) {
            val path = externalCacheDir.path + "/image/image_selector"
            val file = File(path)
            if (file.exists() && file.isFile) {
                file.delete()
            }
            if (!file.exists()) {
                file.mkdirs()
            }
            return file
        }
        val cacheDir = "/Android/data/" + context.packageName + "/cache" + "/image"
        return File(cacheDir)
    }

    fun getExternalCacheDir(context: Context): File? {
        var file = context.externalCacheDir
        if (file == null) {
            val cacheDir = "/Android/data/" + context.packageName + "/cache"
            file = File(Environment.getExternalStorageDirectory().path + cacheDir)
        }
        return file
    }
}