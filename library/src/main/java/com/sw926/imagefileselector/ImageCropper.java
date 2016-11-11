package com.sw926.imagefileselector;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.File;

@SuppressWarnings("unused")
public class ImageCropper {


    public enum CropperErrorResult {
        error,
        canceled,
        notSupport
    }

    private static final String TAG = ImageCropper.class.getSimpleName();

    private static final String IMAGE_CROPPER_BUNDLE = "IMAGE_CROPPER_BUNDLE";

    private int mOutPutX = -1;
    private int mOutPutY = -1;
    private int mAspectX = -1;
    private int mAspectY = -1;
    private boolean mScale = true;

    private File mSrcFile = null;
    private File mOutFile = null;
    private int mRequestCode = -1;

    /**
     * 记录裁切过程中产生的临时文件，裁切完成后进行删除
     */
    private File mTempFile = null;

    private ImageCropperCallback mCallback = null;

    private Fragment fragment = null;
    private Activity activity = null;

    public void setOutPut(int width, int height) {
        mOutPutX = width;
        mOutPutY = height;
    }

    public void setOutPutAspect(int width, int height) {
        mAspectX = width;
        mAspectY = height;
    }

    void setScale(boolean scale) {
        mScale = scale;
    }

    public void setCallback(ImageCropperCallback callback) {
        mCallback = callback;
    }

    private Context getContext() {
        if (activity != null) {
            return activity;
        }
        if (fragment != null) {
            return fragment.getContext();
        }
        return null;
    }

    public void onSaveInstanceState(Bundle outState) {
        Bundle bundle = new Bundle();
        bundle.putInt("outputX", mOutPutX);
        bundle.putInt("outputY", mOutPutY);
        bundle.putInt("aspectX", mAspectX);
        bundle.putInt("aspectY", mAspectY);
        bundle.putBoolean("scale", mScale);
        bundle.putSerializable("outFile", mOutFile);
        bundle.putSerializable("srcFile", mSrcFile);
        bundle.putSerializable("tempFile", mTempFile);
        outState.putBundle(IMAGE_CROPPER_BUNDLE, bundle);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(IMAGE_CROPPER_BUNDLE)) {
            Bundle bundle = savedInstanceState.getBundle(IMAGE_CROPPER_BUNDLE);
            if (bundle != null) {
                mOutPutX = bundle.getInt("outputX");
                mOutPutY = bundle.getInt("outputY");
                mAspectX = bundle.getInt("aspectX");
                mAspectY = bundle.getInt("aspectY");
                mScale = bundle.getBoolean("scale");
                mOutFile = (File) bundle.getSerializable("outFile");
                mSrcFile = (File) bundle.getSerializable("srcFile");
                mTempFile = (File) bundle.getSerializable("tempFile");
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == mRequestCode) {
            if (resultCode == Activity.RESULT_CANCELED) {
                if (mCallback != null) {
                    mCallback.onError(CropperErrorResult.canceled);
                }
                return;
            }
            if (mTempFile != null && mTempFile.exists()) {
                AppLogger.i(TAG, "delete temp file: " + mTempFile.getPath());
                mTempFile.delete();
            }
            if (mOutFile != null && mOutFile.exists()) {
                AppLogger.i(TAG, "crop success output file: " + mOutFile.getPath());
                if (mCallback != null) {
                    mCallback.onSuccess(mOutFile.getPath());
                }
                return;
            }

            Context context = getContext();
            if (context != null && data != null) {
                if (data.getData() != null) {
                    String path = Compatibility.getPath(context, data.getData());
                    if (!TextUtils.isEmpty(path)) {
                        File outputFile = new File(path);
                        if (outputFile.exists()) {
                            AppLogger.i(TAG, "crop success output file:" + path);
                            if (mCallback != null) {
                                mCallback.onSuccess(path);
                            }
                            return;
                        }
                    }
                }

                Bitmap bitmap = data.getParcelableExtra("data");
                if (bitmap != null) {
                    File outputFile = CommonUtils.generateExternalImageCacheFile(context, ".jpg");
                    ImageUtils.saveBitmap(bitmap, outputFile.getPath(), Bitmap.CompressFormat.JPEG, 80);
                    AppLogger.i(TAG, "create output file from data: " + outputFile.getPath());
                    if (mCallback != null) {
                        mCallback.onSuccess(outputFile.getPath());
                    }
                    return;
                }
            }
        }

        if (mCallback != null) {
            mCallback.onError(CropperErrorResult.error);
        }
    }

    public void cropImage(Activity activity, String srcFile, int requestCode) {
        this.fragment = null;
        this.activity = activity;
        this.mRequestCode = requestCode;
        cropImage(srcFile);
    }

    public void cropImage(Fragment fragment, String srcFile, int requestCode) {
        this.fragment = fragment;
        this.activity = null;
        this.mRequestCode = requestCode;
        cropImage(srcFile);
    }

    private void cropImage(String srcFile) {
        try {
            AppLogger.i(TAG, "------------------ start crop file ---------------");

            Context context = getContext();
            if (context == null) {
                AppLogger.e(TAG, "fragment or activity is null");
                if (mCallback != null) {
                    mCallback.onError(CropperErrorResult.error);
                }
                return;
            }

            File inputFile = new File(srcFile);
            if (!inputFile.exists()) {
                AppLogger.e(TAG, "input file not exists");
                if (mCallback != null) {
                    mCallback.onError(CropperErrorResult.error);
                }
                return;
            }

            File outFile = CommonUtils.generateExternalImageCacheFile(context, ".jpg");
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            if (outFile.exists()) {
                outFile.delete();
            }
            AppLogger.i(TAG, "set output file: " + outFile.getPath());


            mSrcFile = inputFile;
            mOutFile = outFile;

            Uri uri;
            if (inputFile.getPath().contains("%")) {
                String ext = srcFile.substring(srcFile.lastIndexOf("."));
                mTempFile = CommonUtils.generateExternalImageCacheFile(context, ext);
                CommonUtils.copy(inputFile, mTempFile);
                uri = FileProvider.getUriForFile(context, "com.sw926.fileprovider", mTempFile);
                AppLogger.w(TAG, "use temp file:" + mTempFile.getPath());
            } else {
                uri = FileProvider.getUriForFile(context, "com.sw926.fileprovider", inputFile);
            }

            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(uri, "image/*");
            intent.putExtra("crop", "true");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (mAspectX > 0 && mAspectY > 0) {
                intent.putExtra("aspectX", mAspectX);
                intent.putExtra("aspectY", mAspectY);
            }
            if (mOutPutX > 0 && mOutPutY > 0) {
                intent.putExtra("outputX", mOutPutX);
                intent.putExtra("outputY", mOutPutY);
            }

            if (mScale) {
                intent.putExtra("scale", true);
                intent.putExtra("scaleUpIfNeeded", true);// 黑边
            }
            if (Compatibility.scaleUpIfNeeded4Black()) {
                intent.putExtra("scaleUpIfNeeded", true);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outFile));
            if (Compatibility.shouldReturnCropData()) {
                intent.putExtra("return-data", true);
            }
            if (activity != null) {
                try {
                    activity.startActivityForResult(intent, mRequestCode);
                } catch (ActivityNotFoundException e) {
                    if (mCallback != null) {
                        mCallback.onError(CropperErrorResult.notSupport);
                    }
                }
            }

            if (fragment != null) {
                try {
                    fragment.startActivityForResult(intent, mRequestCode);
                } catch (ActivityNotFoundException e) {
                    if (mCallback != null) {
                        mCallback.onError(CropperErrorResult.notSupport);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (mCallback != null) {
                mCallback.onError(CropperErrorResult.error);
            }
        }
    }

   public interface ImageCropperCallback {
        void onError(CropperErrorResult result);

        void onSuccess(String outputFile);
    }


}
