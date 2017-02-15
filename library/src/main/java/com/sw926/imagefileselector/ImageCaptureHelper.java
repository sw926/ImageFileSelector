package com.sw926.imagefileselector;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.io.File;

import static com.sw926.imagefileselector.ErrorResult.canceled;
import static com.sw926.imagefileselector.ErrorResult.error;
import static com.sw926.imagefileselector.ErrorResult.permissionDenied;


class ImageCaptureHelper {

    private static final String TAG = "ImageCaptureHelper";

    @Nullable
    private ImageFileSelector.Callback mCallback;

    @Nullable
    private Uri mCameraTempUri;

    private Fragment mFragment = null;
    private Activity mActivity = null;
    private int mRequestCode = -1;

    public int getRequestCode() {
        return mRequestCode;
    }

    public void setCallback(@Nullable ImageFileSelector.Callback callback) {
        mCallback = callback;
    }

    public void onSaveInstanceState(Bundle outState) {
        if (mRequestCode > 0) {
            outState.putInt("image_capture_request_code", mRequestCode);
        }
        if (mCameraTempUri != null) {
            outState.putParcelable("camera_uri", mCameraTempUri);
        }
    }

    public void onRestoreInstanceState(Bundle outState) {
        if (outState.containsKey("image_capture_request_code")) {
            mRequestCode = outState.getInt("image_capture_request_code");
        }
        if (outState.containsKey("camera_uri")) {
            mCameraTempUri = outState.getParcelable("camera_uri");
        }
    }

    public void onActivityResult(Context context, int requestCode, int resultCode, Intent intent) {
        if (context != null)
            if (requestCode == mRequestCode) {
                if (resultCode == Activity.RESULT_CANCELED) {
                    AppLogger.i(TAG, "canceled capture image");
                    if (mCallback != null) {
                        mCallback.onError(canceled);
                    }
                } else if (resultCode == Activity.RESULT_OK) {
                    if (mCameraTempUri != null) {
                        File file = new File(getRealPathFromUri(context, mCameraTempUri));
                        if (file.exists()) {
                            AppLogger.i(TAG, "capture image success: " + file.getPath());
                            if (mCallback != null) {
                                mCallback.onSuccess(file.getPath());
                            }
                        } else {
                            AppLogger.i(TAG, "capture image error " + file.getPath());
                            if (mCallback != null) {
                                mCallback.onError(error);
                            }
                        }
                    }
                }
            }
    }

    public void onRequestPermissionsResult(Context context, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == mRequestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                capture(context);
            } else {
                if (mCallback != null) {
                    mCallback.onError(permissionDenied);
                }
            }
        }
    }

    String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] param = new String[]{MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, param, null, null, null);
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            AppLogger.printStackTrace(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private Intent createIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (mCameraTempUri != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraTempUri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        }
        return intent;
    }

    public void captureImage(Activity activity, int requestCode) {
        this.mRequestCode = requestCode;
        this.mActivity = activity;
        this.mFragment = null;

        if (mActivity != null)
            if (PermissionsHelper.checkAndRequestPermission(mActivity, mRequestCode)) {
                capture(mActivity);
            }
    }

    public void captureImage(Fragment fragment, int requestCode) {
        this.mRequestCode = requestCode;
        this.mActivity = null;
        this.mFragment = fragment;

        if (mFragment != null) {
            if (PermissionsHelper.checkAndRequestPermission(mFragment, mRequestCode)) {
                capture(mFragment.getContext());
            }
        }
    }

    private void capture(Context context) {
        try {
            AppLogger.i(TAG, "start capture image");
            if (context != null) {
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
                mCameraTempUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }

            if (mActivity != null) {
                mActivity.startActivityForResult(createIntent(), mRequestCode);
                return;
            }

            if (mFragment != null) {
                mFragment.startActivityForResult(createIntent(), mRequestCode);
            }

        } catch (ActivityNotFoundException e) {
            AppLogger.printStackTrace(e);
        }
    }
}
