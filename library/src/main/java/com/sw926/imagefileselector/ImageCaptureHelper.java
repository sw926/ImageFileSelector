package com.sw926.imagefileselector;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;

@SuppressWarnings("unused")
class ImageCaptureHelper {

    private static final String KEY_OUT_PUT_FILE = "key_out_put_file";
    private static final int CHOOSE_PHOTO_FROM_CAMERA = 0x702;

    private File mOutFile;
    private Callback mCallback;

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void onSaveInstanceState(Bundle outState) {
        if (mOutFile != null) {
            outState.putString(KEY_OUT_PUT_FILE, mOutFile.getPath());
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String tempFilePath = savedInstanceState.getString(KEY_OUT_PUT_FILE);
            if (!TextUtils.isEmpty(tempFilePath)) {
                mOutFile = new File(tempFilePath);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_PHOTO_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            if (mOutFile != null && mOutFile.exists()) {
                if (mCallback != null) {
                    mCallback.onSuccess(mOutFile.getPath());
                }
            } else {
                if (mCallback != null) {
                    mCallback.onSuccess(null);
                }
            }
        }
    }

    public void captureImage(Activity activity) {
        mOutFile = CommonUtils.generateExternalImageCacheFile(activity, ".jpg");
        try {
            activity.startActivityForResult(createIntent(), CHOOSE_PHOTO_FROM_CAMERA);
        } catch (ActivityNotFoundException e) {
            AppLogger.printStackTrace(e);
            if (mCallback != null) {
                mCallback.onError();
            }
        }
    }

    public void captureImage(android.support.v4.app.Fragment fragment) {
        mOutFile = CommonUtils.generateExternalImageCacheFile(fragment.getContext(), ".jpg");
        try {
            fragment.startActivityForResult(createIntent(), CHOOSE_PHOTO_FROM_CAMERA);
        } catch (ActivityNotFoundException e) {
            AppLogger.printStackTrace(e);
            if (mCallback != null) {
                mCallback.onError();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void captureImage(android.app.Fragment fragment) {
        mOutFile = CommonUtils.generateExternalImageCacheFile(fragment.getActivity(), ".jpg");
        try {
            fragment.startActivityForResult(createIntent(), CHOOSE_PHOTO_FROM_CAMERA);
        } catch (ActivityNotFoundException e) {
            AppLogger.printStackTrace(e);
            if (mCallback != null) {
                mCallback.onError();
            }
        }
    }

    private Intent createIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mOutFile));
        return intent;
    }

    public interface Callback {

        void onSuccess(String fileName);

        void onError();
    }
}
