package com.sw926.imagefileselector;

import android.graphics.Bitmap;
import android.os.Build;

/**
 * Created by sunwei on 15/11/16.
 */
public class CompressFormatUtils {

    public static Bitmap.CompressFormat parseFormat(String fileName) {

        int dotPos = fileName.lastIndexOf(".");
        if (dotPos <= 0) {
            return Bitmap.CompressFormat.JPEG;
        }
        String ext = fileName.substring(dotPos + 1);
        if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg")) {
            return Bitmap.CompressFormat.JPEG;
        }
        if (ext.equalsIgnoreCase("png")) {
            return Bitmap.CompressFormat.PNG;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (ext.equalsIgnoreCase("webp")) {
                return Bitmap.CompressFormat.WEBP;
            }
        }
        return Bitmap.CompressFormat.JPEG;
    }

    public static String getExt(Bitmap.CompressFormat format) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (format == Bitmap.CompressFormat.WEBP) {
                return ".webp";
            }
        }

        if (format == Bitmap.CompressFormat.PNG) {
            return ".png";
        }
        return ".jpg";
    }

    public static String getExt(String fileName) {
        Bitmap.CompressFormat format = parseFormat(fileName);
        return getExt(format);
    }
}
