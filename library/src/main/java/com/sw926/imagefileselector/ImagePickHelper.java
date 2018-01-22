package com.sw926.imagefileselector;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import java.io.File;

import static com.sw926.imagefileselector.ErrorResult.error;
import static com.sw926.imagefileselector.ErrorResult.permissionDenied;

class ImagePickHelper {

    private static final String TAG = "ImagePickHelper";

    private final Context mContext;

    private ImageFileSelector.Callback mCallback;
    @Nullable
    private Fragment mFragment = null;
    @Nullable
    private Activity mActivity = null;
    private int mRequestCode = -1;

    private String mType = "image/*";

    private PermissionsHelper mPermissionsHelper = new PermissionsHelper();

    private PermissionsHelper.Callback mPermissionCallback = new PermissionsHelper.Callback() {
        @Override
        public void onRequestPermissionsCallback(boolean isGranted) {
            if (isGranted) {
                startSelect();
            } else {
                if (mCallback != null) {
                    mCallback.onError(permissionDenied);
                }
            }
        }
    };

    ImagePickHelper(Context context) {
        mContext = context;
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    public void setType(String type) {
        mType = type;
    }

    public void setCallback(ImageFileSelector.Callback callback) {
        mCallback = callback;
    }

    public void selectImage(@NonNull Fragment fragment, int requestCode) {
        AppLogger.i(TAG, "start select image from fragment");
        mRequestCode = requestCode;
        this.mFragment = fragment;
        this.mActivity = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mPermissionsHelper.checkAndRequestPermission(mFragment, mRequestCode, mPermissionCallback, Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            startSelect();
        }
    }

    public void selectorImage(@NonNull Activity activity, int requestCode) {
        AppLogger.i(TAG, "start select image from activity");
        mRequestCode = requestCode;
        this.mActivity = activity;
        this.mFragment = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mPermissionsHelper.checkAndRequestPermission(mActivity, mRequestCode, mPermissionCallback, Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            startSelect();
        }
    }

    private void startSelect() {
        AppLogger.i(TAG, "start system gallery activity");
        if (mActivity != null) {
            try {
                mActivity.startActivityForResult(createIntent(), mRequestCode);
            } catch (Exception e) {
                if (mCallback != null) {
                    mCallback.onError(error);
                }
            }
            return;
        }

        if (mFragment != null) {
            try {
                mFragment.startActivityForResult(createIntent(), mRequestCode);
            } catch (Exception e) {
                if (mCallback != null) {
                    mCallback.onError(error);
                }
            }
            return;
        }
        AppLogger.e(TAG, "activity or fragment is null");
        if (mCallback != null) {
            mCallback.onError(error);
        }
    }

    private Intent createIntent() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mType);
        return intent;
    }


    public void onSaveInstanceState(Bundle outState) {
        if (mRequestCode > 0) {
            outState.putInt("image_pick_request_code", mRequestCode);
        }
    }

    public void onRestoreInstanceState(@Nullable Bundle outState) {
        if (outState != null && outState.containsKey("image_pick_request_code")) {
            mRequestCode = outState.getInt("image_pick_request_code");
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == mRequestCode) {
            if (resultCode == Activity.RESULT_CANCELED) {
                AppLogger.i(TAG, "canceled select image");
                if (mCallback != null) {
                    mCallback.onError(ErrorResult.canceled);
                }
            } else if (resultCode == Activity.RESULT_OK) {
                if (intent == null) {
                    AppLogger.e(TAG, "select image error, intent null");
                    if (mCallback != null) {
                        mCallback.onError(error);
                    }
                } else {
                    Uri uri = intent.getData();
                    String path = Compatibility.getPath(mContext, uri);
                    if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                        AppLogger.i(TAG, "select image success: " + path);
                        if (mCallback != null) {
                            mCallback.onSuccess(path);
                        }
                    } else {
                        AppLogger.e(TAG, "select image file path " + path + " is error or not exists");
                        if (mCallback != null) {
                            mCallback.onError(error);
                        }
                    }
                }

            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mPermissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
