package com.sw926.imagefileselector;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class ImageUtils {

    static void saveBitmap(Bitmap bmp, String filePath, Bitmap.CompressFormat format, int quality) {
        FileOutputStream fo;
        try {
            File f = new File(filePath);
            if (!f.getParentFile().exists()) {
                //noinspection ResultOfMethodCallIgnored
                f.getParentFile().mkdirs();
            }

            if (f.exists()) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
            //noinspection ResultOfMethodCallIgnored
            f.createNewFile();
            fo = new FileOutputStream(f, true);
            bmp.compress(format, quality, fo);
            fo.flush();
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            AppLogger.INSTANCE.printStackTrace(ex);
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }

            }
        }
        return degree;
    }

    static Bitmap rotateImage(int angle, Bitmap bitmap) {
        try {
            // 旋转图片 动作
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            // 创建新的图片
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                    true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return null;
    }

}
