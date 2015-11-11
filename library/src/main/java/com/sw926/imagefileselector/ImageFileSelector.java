package com.sw926.imagefileselector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.io.File;

@SuppressWarnings("unused")
public class ImageFileSelector {

    private static final String TAG = ImageFileSelector.class.getSimpleName();

    private Callback mCallback;
    private ImagePickHelper mImagePickHelper;
    private ImageCaptureHelper mImageTaker;
    private ImageCompressHelper mImageCompressHelper;

    public ImageFileSelector(final Context context) {
        mImagePickHelper = new ImagePickHelper(context);
        mImagePickHelper.setCallback(new ImagePickHelper.Callback() {
            @Override
            public void onSuccess(String file) {
                AppLogger.d(TAG, "select image from sdcard: " + file);
                handleResult(file, false);
            }

            @Override
            public void onError() {
                handleError();
            }
        });

        mImageTaker = new ImageCaptureHelper();
        mImageTaker.setCallback(new ImageCaptureHelper.Callback() {
            @Override
            public void onSuccess(String file) {
                AppLogger.d(TAG, "select image from camera: " + file);
                handleResult(file, true);
            }

            @Override
            public void onError() {
                handleError();
            }
        });

        mImageCompressHelper = new ImageCompressHelper(context);
        mImageCompressHelper.setCallback(new ImageCompressHelper.CompressCallback() {
            @Override
            public void onCallBack(String outFile) {
                AppLogger.d(TAG, "compress image output: " + outFile);
                if (mCallback != null) {
                    mCallback.onSuccess(outFile);
                }
            }
        });
    }

    public static void setDebug(boolean debug) {
        AppLogger.DEBUG = debug;
    }

    /**
     * 设置压缩后的文件大小
     *
     * @param maxWidth  压缩后文件宽度
     * @param maxHeight 压缩后文件高度
     */
    @SuppressWarnings("unused")
    public void setOutPutImageSize(int maxWidth, int maxHeight) {
        mImageCompressHelper.setOutPutImageSize(maxWidth, maxHeight);
    }

    /**
     * 设置压缩后保存图片的质量
     *
     * @param quality 图片质量 0 - 100
     */
    @SuppressWarnings("unused")
    public void setQuality(int quality) {
        mImageCompressHelper.setQuality(quality);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mImagePickHelper.onActivityResult(requestCode, resultCode, data);
        mImageTaker.onActivityResult(requestCode, resultCode, data);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mImagePickHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onSaveInstanceState(Bundle outState) {
        mImageTaker.onSaveInstanceState(outState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mImageTaker.onRestoreInstanceState(savedInstanceState);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void selectImage(Activity activity) {
        mImagePickHelper.selectorImage(activity);
    }

    public void takePhoto(Activity activity) {
        mImageTaker.captureImage(activity);
    }

    private void handleResult(String fileName, boolean deleteSrc) {
        File file = new File(fileName);
        if (file.exists()) {
            mImageCompressHelper.compress(fileName, deleteSrc);
        } else {
            if (mCallback != null) {
                mCallback.onSuccess(null);
            }
        }
    }

    private void handleError() {
        if (mCallback != null) {
            mCallback.onError();
        }
    }

    public interface Callback {
        void onSuccess(String file);

        void onError();
    }

}
