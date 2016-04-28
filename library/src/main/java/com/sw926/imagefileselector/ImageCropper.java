package com.sw926.imagefileselector;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;

@SuppressWarnings("unused")
public class ImageCropper {

    private static final String TAG = ImageCropper.class.getSimpleName();

    private static final String IMAGE_CROPPER_BOUDLE = "image_cropper_boudle";
    private static final int CROP_PHOTO_SMALL = 2903;

    public enum CropperResult {
        /**
         * 裁切成功
         */
        success,
        /**
         * 输入文件错误
         */
        error_illegal_input_file,
        /**
         * 输出文件错误
         */
        error_illegal_out_file
    }

    private Object mHolder = null;

    private int mOutPutX = -1;
    private int mOutPutY = -1;
    private int mAspectX = -1;
    private int mAspectY = -1;
    private boolean mScale = true;

    private File mSrcFile;
    private File mOutFile;

    /**
     * 记录裁切过程中产生的临时文件，裁切完成后进行删除
     */
    private File mTempFile;

    private ImageCropperCallback mCallback;

    public ImageCropper(android.support.v4.app.Fragment fragment) {
        mHolder = fragment;
    }

    public ImageCropper(android.app.Fragment fragment) {
        mHolder = fragment;
    }

    public ImageCropper(Activity activity) {
        mHolder = activity;
    }

    public void setOutPut(int width, int height) {
        mOutPutX = width;
        mOutPutY = height;
    }

    public void setOutPutAspect(int width, int height) {
        mAspectX = width;
        mAspectY = height;
    }

    public void setScale(boolean scale) {
        mScale = scale;
    }

    public void setCallback(ImageCropperCallback callback) {
        mCallback = callback;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private Context getContext() {
        if (mHolder != null) {
            if (mHolder instanceof Activity) {
                return (Context) mHolder;
            } else if (mHolder instanceof android.support.v4.app.Fragment) {
                return ((android.support.v4.app.Fragment) mHolder).getActivity();
            } else if (mHolder instanceof android.app.Fragment) {
                return ((android.app.Fragment) mHolder).getActivity();
            }
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
        outState.putBundle(IMAGE_CROPPER_BOUDLE, bundle);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(IMAGE_CROPPER_BOUDLE)) {
            Bundle bundle = savedInstanceState.getBundle(IMAGE_CROPPER_BOUDLE);
            if (bundle != null) {
                mOutPutX = bundle.getInt("outgetX");
                mOutPutY = bundle.getInt("outgetY");
                mAspectX = bundle.getInt("aspectX");
                mAspectY = bundle.getInt("aspectY");
                mScale = bundle.getBoolean("scale");
                mOutFile = (File) bundle.getSerializable("outFile");
                mSrcFile = (File) bundle.getSerializable("srcFile");
                mTempFile = (File) bundle.getSerializable("tempFile");
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == CROP_PHOTO_SMALL) {
            if (mTempFile != null && mTempFile.exists()) {
                AppLogger.i(TAG, "delete temp file:" + mTempFile.getPath());
                mTempFile.delete();
            }
            File outFile = null;
            if (mOutFile != null && mOutFile.exists()) {
                AppLogger.i(TAG, "use output file:" + mOutFile.getPath());
                outFile = mOutFile;
            } else if (data.getData() != null) {
                String outFilePath = Compatibility.getPath(getContext(), data.getData());
                AppLogger.i(TAG, "get output file from uri:" + outFilePath);
                if (!TextUtils.isEmpty(outFilePath)) {
                    outFile = new File(outFilePath);
                    AppLogger.i(TAG, "output file exists:" + outFile.getPath());
                }
            } else {
                outFile = CommonUtils.generateExternalImageCacheFile(getContext(), ".jpg");
                Bitmap bitmap = data.getParcelableExtra("data");
                if (bitmap != null) {
                    AppLogger.i(TAG, "create output file from data:" + outFile.getPath());
                    ImageUtils.saveBitmap(bitmap, outFile.getPath(), CompressFormat.JPEG, 80);
                }
            }
            if (outFile != null && outFile.exists()) {
                AppLogger.i(TAG, "crop file success, output file:" + outFile.getPath());
                if (mCallback != null) {
                    mCallback.onCropperCallback(CropperResult.success, mSrcFile, mOutFile);
                }
            } else {
                AppLogger.i(TAG, "crop file error: output file not exists");
                if (mCallback != null) {
                    mCallback.onCropperCallback(CropperResult.error_illegal_out_file, mSrcFile, null);
                }
            }
            AppLogger.i(TAG, "------------------ end crop file ---------------");
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void cropImage(File srcFile) {

        AppLogger.i(TAG, "------------------ start crop file ---------------");
        if (!(srcFile != null && srcFile.exists())) {
            AppLogger.i(TAG, "input file null or not exists ");
            if (mCallback != null) {
                mCallback.onCropperCallback(CropperResult.error_illegal_input_file, srcFile, null);
            }
            return;
        }

        File outFile = CommonUtils.generateExternalImageCacheFile(getContext(), ".jpg");
        AppLogger.i(TAG, "output file:" + outFile.getPath());
        if (outFile.exists()) {
            outFile.delete();
        }
        if (!outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }

        mSrcFile = srcFile;
        mOutFile = outFile;
        Uri uri = Uri.fromFile(srcFile);

        if (uri.toString().contains("%")) {

            String inputFileName = srcFile.getName();
            String ext = inputFileName.substring(inputFileName.lastIndexOf("."));
            mTempFile = CommonUtils.generateExternalImageCacheFile(getContext(), ext);
            CommonUtils.copy(srcFile, mTempFile);
            uri = Uri.fromFile(mTempFile);
            AppLogger.w(TAG, "use temp file:" + mTempFile.getPath());
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");

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

        if (mHolder == null) {
            throw new NullPointerException("'mHolder' is null.");
        }
        if (mHolder instanceof Activity) {
            ((Activity) mHolder).startActivityForResult(intent, CROP_PHOTO_SMALL);
        } else if (mHolder instanceof android.support.v4.app.Fragment) {
            ((android.support.v4.app.Fragment) mHolder).startActivityForResult(intent, CROP_PHOTO_SMALL);
        } else if (mHolder instanceof android.app.Fragment) {
            ((android.app.Fragment) mHolder).startActivityForResult(intent, CROP_PHOTO_SMALL);
        }
    }

    public interface ImageCropperCallback {
        void onCropperCallback(CropperResult result, File srcFile, File outFile);
    }


}
