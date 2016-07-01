package com.sw926.imagefileselector;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import java.io.File;
import java.lang.ref.WeakReference;


class ImagePickHelper {

    private static final int SELECT_PIC = 0x701;
    public static final int IMAGE_PICK_HELPER_REQUEST_PERMISSIONS = 0x11;

    private Callback mCallback;
    private Context mContext;
    private WeakReference mWeakReference;

    public ImagePickHelper(Context context) {
        mContext = context;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void selectImage(android.app.Fragment fragment) {
        mWeakReference = new WeakReference(fragment);
        if (PermissionsHelper.checkAndRequestPermission(fragment.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE, IMAGE_PICK_HELPER_REQUEST_PERMISSIONS)) {
            doSelect(fragment);
        }
    }

    public void selectImage(android.support.v4.app.Fragment fragment) {
        mWeakReference = new WeakReference(fragment);
        if (PermissionsHelper.checkAndRequestPermission(fragment.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE, IMAGE_PICK_HELPER_REQUEST_PERMISSIONS)) {
            doSelect(fragment);
        }
    }

    public void selectorImage(Activity activity) {
        mWeakReference = new WeakReference(activity);
        if (PermissionsHelper.checkAndRequestPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE, IMAGE_PICK_HELPER_REQUEST_PERMISSIONS)) {
            doSelect(activity);
        }
    }

    private void doSelect(Object object) {
        Intent intent = createIntent();
        CommonUtils.startActivityForResult(object, intent, SELECT_PIC);
    }

    private Intent createIntent() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        return intent;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == SELECT_PIC) {
            Uri uri = intent.getData();
            String path = Compatibility.getPath(mContext, uri);
            if (mCallback != null) {
                if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                    mCallback.onSuccess(path);
                } else {
                    mCallback.onError();
                }
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == IMAGE_PICK_HELPER_REQUEST_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mWeakReference != null) {
                    Object holder = mWeakReference.get();
                    if (holder != null) {
                        doSelect(holder);
                        return;
                    }
                }
            }
            mCallback.onError();
        }
    }

    public interface Callback {
        void onSuccess(String file);

        void onError();
    }


}
