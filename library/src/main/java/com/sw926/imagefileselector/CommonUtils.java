package com.sw926.imagefileselector;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.Fragment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class CommonUtils {

    public static void startActivityForResult(Object object, Intent intent, int requestCode) {
        if (object != null) {
            if (object instanceof Activity) {
                startActivityForResult((Activity) object, intent, requestCode);
            } else if (object instanceof Fragment) {
                startActivityForResult((Fragment) object, intent, requestCode);
            } else if (object instanceof android.app.Fragment) {
                startActivityForResult((android.app.Fragment) object, intent, requestCode);
            }
        }
    }

    public static void startActivityForResult(Activity activity, Intent intent, int requestCode) {
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startActivityForResult(Fragment fragment, Intent intent, int requestCode) {
        fragment.startActivityForResult(intent, requestCode);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void startActivityForResult(android.app.Fragment fragment, Intent intent, int requestCode) {
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * copy file
     *
     * @param source source file
     * @param dest   dest file
     * @return true if success copied
     */
    public static boolean copy(File source, File dest) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        boolean result = true;
        try {
            bis = new BufferedInputStream(new FileInputStream(source));
            bos = new BufferedOutputStream(new FileOutputStream(dest, false));

            byte[] buf = new byte[1024];
            bis.read(buf);

            do {
                bos.write(buf);
            } while (bis.read(buf) != -1);
        } catch (IOException e) {
            result = false;
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                result = false;
            }
        }

        return result;
    }

    /**
     * 在图片缓存文件夹生成一个临时文件
     *
     * @param context context
     * @param ext     文件后缀名 e.g ".jpg"
     * @return 生成的临时文件
     */
    public static File generateExternalImageCacheFile(Context context, String ext) {
        String fileName = "img_" + System.currentTimeMillis();
        return generateExternalImageCacheFile(context, fileName, ext);
    }

    private static File generateExternalImageCacheFile(Context context, String fileName, String ext) {
        File cacheDir = getExternalImageCacheDir(context);
        String path = cacheDir.getPath() + File.separator + fileName + ext;
        return new File(path);
    }

    public static File getExternalImageCacheDir(Context context) {
        File externalCacheDir = getExternalCacheDir(context);
        if (externalCacheDir != null) {
            String path = externalCacheDir.getPath() + "/image/image_selector";
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            if (!file.exists()) {
                file.mkdirs();
            }
            return file;
        }
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache" + "/image";
        return new File(cacheDir);
    }

    public static File getExternalCacheDir(Context context) {
        File file = context.getExternalCacheDir();
        if (file == null) {
            final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache";
            file = new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
        }
        return file;
    }
}