package com.sw926.imagefileselector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

@SuppressWarnings("unused")
public class ImageFileSelector {

    @Nullable
    private Callback mCallback = null;
    private ImagePickHelper mImagePickHelper;
    private ImageCaptureHelper mImageCaptureHelper;
    private ImageCompressHelper mImageCompressHelper;

    public ImageCompressHelper.CompressParams compressParams;

    public ImageFileSelector(Context context) {

        String defaultOutputPath = context.getExternalCacheDir() + "/images/";
        compressParams = new ImageCompressHelper.CompressParams();
        compressParams.outputPath = defaultOutputPath;

        mImageCompressHelper = new ImageCompressHelper();
        mImageCompressHelper.setCallback(new ImageCompressHelper.Callback() {
            @Override
            public void onError() {
                if (mCallback != null) {
                    mCallback.onError(ErrorResult.error);
                }
            }

            @Override
            public void onSuccess(String file) {
                if (mCallback != null) {
                    mCallback.onSuccess(file);
                }

            }
        });

        Callback callback = new Callback() {
            @Override
            public void onError(ErrorResult errorResult) {
                if (mCallback != null) {
                    mCallback.onError(errorResult);
                }
            }

            @Override
            public void onSuccess(String file) {
                if (mCallback != null) {
                    mCallback.onSuccess(file);
                }
            }
        };

        mImagePickHelper = new ImagePickHelper(context);
        mImagePickHelper.setCallback(callback);

        mImageCaptureHelper = new ImageCaptureHelper();
        mImageCaptureHelper.setCallback(callback);

    }


    void setOutPutPath(String outPutPath) {
        compressParams.outputPath = outPutPath;
    }

    void setSelectFileType(String type) {
        mImagePickHelper.setType(type);
    }

    /**
     * 设置压缩后的文件大小
     *
     * @param maxWidth  压缩后文件宽度
     *                  *
     * @param maxHeight 压缩后文件高度
     */
    @SuppressWarnings("unused")
    public void setOutPutImageSize(int maxWidth, int maxHeight) {
        compressParams.maxWidth = maxWidth;
        compressParams.maxHeight = maxHeight;
    }

    /**
     * 设置压缩后保存图片的质量
     *
     * @param quality 图片质量 0 - 100
     */
    @SuppressWarnings("unused")
    void setQuality(int quality) {
        compressParams.saveQuality = quality;
    }

    /**
     * set image compress format
     *
     * @param compressFormat compress format
     */
    @SuppressWarnings("unused")
    void setCompressFormat(Bitmap.CompressFormat compressFormat) {
        compressParams.compressFormat = compressFormat;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mImagePickHelper.onActivityResult(requestCode, resultCode, data);
        mImageCaptureHelper.onActivityResult(requestCode, resultCode, data);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == mImagePickHelper.getRequestCode()) {
            mImagePickHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else if (requestCode == mImageCaptureHelper.getRequestCode()) {
            mImageCaptureHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
    }

    public void onRestoreInstanceState(Bundle outState) {
    }

    public void setCallback(@Nullable Callback callback) {
        mCallback = callback;
    }

    public void selectImage(Activity activity, int requestCode) {
        mImagePickHelper.selectorImage(activity, requestCode);
    }

   public void selectImage(Fragment fragment, int requestCode) {
        mImagePickHelper.selectImage(fragment, requestCode);
    }

    public void takePhoto(Activity activity, int requestCode) {
        mImageCaptureHelper.captureImage(activity, requestCode);
    }

    public void takePhoto(Fragment fragment, int requestCode) {
        mImageCaptureHelper.captureImage(fragment, requestCode);
    }


    public static void setDebug(Boolean debug) {
        AppLogger.DEBUG = debug;
    }

    /**
     * Created by sunwei on 10/11/2016 2:36 PM.
     */

    public interface Callback {
        void onError(ErrorResult errorResult);
        void onSuccess(String file);
    }
}
