package com.sw926.imagefileselector;


import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.File;

import static com.sw926.imagefileselector.ErrorResult.canceled;
import static com.sw926.imagefileselector.ErrorResult.error;
import static com.sw926.imagefileselector.ErrorResult.permissionDenied;


public class ImageCaptureHelper {

    private static final String TAG = "ImageCaptureHelper";

    @Nullable
    private ImageFileSelector.Callback mCallback;

    private final PermissionsHelper mPermissionsHelper = new PermissionsHelper();
    private final PermissionsHelper.Callback mPermissionCallback = new PermissionsHelper.Callback() {
        @Override
        public void onRequestPermissionsCallback(boolean isGranted) {
            if (isGranted) {
                capture();
            } else {
                if (mCallback != null) {
                    mCallback.onError(permissionDenied);
                }
            }
        }
    };

    @Nullable
    private File mOutputFile;

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
        if (outState != null) {
            if (mRequestCode > 0) {
                outState.putInt("image_capture_request_code", mRequestCode);
            }
            if (mOutputFile != null) {
                outState.putString("output_file", mOutputFile.getPath());
            }
        }
    }

    public void onRestoreInstanceState(@Nullable Bundle outState) {
        if (outState != null) {
            if (outState.containsKey("image_capture_request_code")) {
                mRequestCode = outState.getInt("image_capture_request_code");
            }
            if (outState.containsKey("output_file")) {
                String outputFilePath = outState.getString("output_file");
                if (!TextUtils.isEmpty(outputFilePath)) {
                    mOutputFile = new File(outputFilePath);
                }
            }
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
                    if (mOutputFile != null) {
                        if (mOutputFile.exists()) {
                            AppLogger.i(TAG, "capture image success: " + mOutputFile.getPath());
                            if (mCallback != null) {
                                mCallback.onSuccess(mOutputFile.getPath());
                            }
                        } else {
                            AppLogger.i(TAG, "capture image error " + mOutputFile.getPath());
                            if (mCallback != null) {
                                mCallback.onError(error);
                            }
                        }
                    }
                }
            }
    }

    public void onRequestPermissionsResult(Context context, int requestCode, String[] permissions, int[] grantResults) {
        mPermissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == mRequestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                capture();
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
        if (mOutputFile != null) {
            Uri cameraTempUri = FileProvider.getUriForFile(mActivity, mActivity.getApplicationContext().getPackageName() + ".com.sw926.imagefileselector.provider", mOutputFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraTempUri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        }
        return intent;
    }

    public void captureImage(Activity activity, int requestCode) {
        this.mRequestCode = requestCode;
        this.mActivity = activity;
        this.mFragment = null;

        if (mActivity != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mPermissionsHelper.checkAndRequestPermission(mActivity, requestCode, mPermissionCallback, Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                capture();
            }
        }
    }

    public void captureImage(Fragment fragment, int requestCode) {
        this.mRequestCode = requestCode;
        this.mActivity = null;
        this.mFragment = fragment;

        if (mFragment != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mPermissionsHelper.checkAndRequestPermission(mFragment, requestCode, mPermissionCallback, Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                capture();
            }
        }
    }

    private void capture() {
        if (!CommonUtils.hasSDCardMounted()) {
            if (mCallback != null) {
                mCallback.onError(ErrorResult.error);
            }
            return;
        }

        try {
            AppLogger.i(TAG, "start capture image");

            Context context = null;
            if (mActivity != null) {
                context = mActivity;
            } else if (mFragment != null) {
                context = mFragment.getContext();
            }

            if (context != null) {
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");

                String fileName = "img_" + System.currentTimeMillis() + ".jpg";
                mOutputFile = new File(context.getExternalCacheDir(), fileName);
                AppLogger.d(TAG, "capture ouput file: " + mOutputFile);
            }

            if (mActivity != null) {
                mActivity.startActivityForResult(createIntent(), mRequestCode);
                return;
            }

            if (mFragment != null) {
                mFragment.startActivityForResult(createIntent(), mRequestCode);
            }

        } catch (Throwable e) {
            if (mCallback != null) {
                mCallback.onError(ErrorResult.error);
            }
            AppLogger.printStackTrace(e);
        }
    }
}
