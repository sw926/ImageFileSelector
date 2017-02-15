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
                ImageCompressHelper.CompressJop jop = new ImageCompressHelper.CompressJop();
                jop.params = compressParams;
                jop.inputFile = file;
                mImageCompressHelper.compress(jop);
            }
        };

        mImagePickHelper = new ImagePickHelper(context);
        mImagePickHelper.setCallback(callback);

        mImageCaptureHelper = new ImageCaptureHelper();
        mImageCaptureHelper.setCallback(callback);
    }


    public void setOutPutPath(String outPutPath) {
        compressParams.outputPath = outPutPath;
    }

    public void setSelectFileType(String type) {
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
    public void setQuality(int quality) {
        compressParams.saveQuality = quality;
    }

    /**
     * set image compress format
     *
     * @param compressFormat compress format
     */
    @SuppressWarnings("unused")
    public void setCompressFormat(Bitmap.CompressFormat compressFormat) {
        compressParams.compressFormat = compressFormat;
    }

    public void onActivityResult(Context context, int requestCode, int resultCode, Intent data) {
        mImagePickHelper.onActivityResult(requestCode, resultCode, data);
        mImageCaptureHelper.onActivityResult(context, requestCode, resultCode, data);
    }

    public void onRequestPermissionsResult(Context context, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == mImagePickHelper.getRequestCode()) {
            mImagePickHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else if (requestCode == mImageCaptureHelper.getRequestCode()) {
            mImageCaptureHelper.onRequestPermissionsResult(context, requestCode, permissions, grantResults);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        if (mImageCaptureHelper != null) {
            mImageCaptureHelper.onSaveInstanceState(outState);
        }
        if (mImagePickHelper != null) {
            mImagePickHelper.onSaveInstanceState(outState);
        }
    }

    public void onRestoreInstanceState(Bundle outState) {
        if (mImageCaptureHelper != null) {
            mImageCaptureHelper.onRestoreInstanceState(outState);
        }
        if (mImagePickHelper != null) {
            mImagePickHelper.onRestoreInstanceState(outState);
        }
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
