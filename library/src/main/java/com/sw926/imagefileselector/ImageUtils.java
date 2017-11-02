package com.sw926.imagefileselector;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtils {

    @Nullable
    public static Bitmap compressImageFile(@NonNull String srcFile, int maxWidth, int maxHeight) {
        Bitmap bitmap = null;

        long inputFileLength = new File(srcFile).length();
        AppLogger.i(ImageCompressHelper.TAG, "compress file:" + srcFile);
        AppLogger.i(ImageCompressHelper.TAG, "file length:" + (inputFileLength / 1024.0) + "kb");
        AppLogger.i(ImageCompressHelper.TAG, "max output size:(" + maxWidth + "," + maxHeight);

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(srcFile, decodeOptions);
        int actualWidth = decodeOptions.outWidth;
        int actualHeight = decodeOptions.outHeight;

        AppLogger.i(ImageCompressHelper.TAG, "input size:(" + actualWidth + ", " + actualHeight);

        if (actualHeight <= maxHeight && actualWidth <= maxWidth) {
            AppLogger.w(ImageCompressHelper.TAG, "no need to compress: input size < output size");
            decodeOptions.inJustDecodeBounds = false;
            try {
                bitmap = BitmapFactory.decodeFile(srcFile);
            } catch (OutOfMemoryError e) {
                AppLogger.printStackTrace(e);
                AppLogger.e(ImageCompressHelper.TAG, "OutOfMemoryError:" + srcFile + ", size:" + actualWidth + "," + actualHeight);
            }

            if (bitmap != null) {
                int degree = ImageUtils.getExifOrientation(srcFile);
                if (degree != 0) {
                    AppLogger.w(ImageCompressHelper.TAG, "rotate image from: " + degree);
                    bitmap = rotateImageNotNull(degree, bitmap);
                }
            }
            return bitmap;
        }

        int sampleSize;
        int w;
        int h;
        if (actualWidth * maxHeight > maxWidth * actualHeight) {
            w = maxWidth;
            h = (int) (w * actualHeight / (double) actualWidth);
            sampleSize = (int) (actualWidth / (double) maxWidth);
        } else {
            h = maxHeight;
            w = (int) (h * actualWidth / (double) actualHeight);
            sampleSize = (int) (actualHeight / (double) maxHeight);
        }

        AppLogger.i(ImageCompressHelper.TAG, "in simple size:" + sampleSize);

        decodeOptions.inJustDecodeBounds = false;
        decodeOptions.inSampleSize = sampleSize;
        decodeOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        decodeOptions.inPurgeable = true;
        decodeOptions.inInputShareable = true;
        try {
            bitmap = BitmapFactory.decodeFile(srcFile, decodeOptions);
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
            AppLogger.e(ImageCompressHelper.TAG, "OutOfMemoryError:" + srcFile + ", size:" + actualWidth + "," + actualHeight);
        }

        if (bitmap != null) {
            AppLogger.i(ImageCompressHelper.TAG, "bitmap size after decode:(" + bitmap.getWidth() + ", " + bitmap.getHeight() + ")");

            if (bitmap.getWidth() > maxWidth || bitmap.getHeight() > maxHeight) {
                Bitmap tempBitmap = null;
                try {
                    tempBitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
                } catch (OutOfMemoryError e) {
                    AppLogger.printStackTrace(e);
                }
                if (tempBitmap != null) {
                    bitmap.recycle();
                    bitmap = tempBitmap;
                    AppLogger.i(ImageCompressHelper.TAG, "scale down:(" + bitmap.getWidth() + ", " + bitmap.getHeight() + ")");
                }
            }

            int degree = ImageUtils.getExifOrientation(srcFile);
            if (degree != 0) {
                AppLogger.i(ImageCompressHelper.TAG, "rotate image from: " + degree);
                bitmap = rotateImageNotNull(degree, bitmap);
            }
            AppLogger.i(ImageCompressHelper.TAG, "output file width: " + bitmap.getWidth() + ", height: " + bitmap.getHeight());
        }

        return bitmap;
    }

    public static void saveBitmap(Bitmap bmp, String filePath, Bitmap.CompressFormat format, int quality) {
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

    public static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException e) {
            AppLogger.printStackTrace(e);
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

    private static Bitmap rotateImageNotNull(int angle, Bitmap bitmap) {
        Bitmap out = null;
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            out = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            AppLogger.printStackTrace(e);
            AppLogger.e(ImageCompressHelper.TAG, "rotate image error, image will not display in current orientation");
        }
        if (out != null) {
            bitmap.recycle();
            return out;
        }
        return bitmap;
    }

}
