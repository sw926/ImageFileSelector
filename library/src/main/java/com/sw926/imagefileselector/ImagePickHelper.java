package com.sw926.imagefileselector;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.lang.ref.WeakReference;


class ImagePickHelper {

    private static final int SELECT_PIC = 0x701;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 0x11;

    private Callback mCallback;
    private Context mContext;

    private WeakReference mWeakReference;

    public ImagePickHelper(Context context) {
        mContext = context;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void selectImage(android.app.Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(fragment.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                mWeakReference = new WeakReference<>(fragment);
                fragment.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_REQUEST_CODE);

            } else {
                doSelect(fragment);
            }
        } else {
            doSelect(fragment);
        }
    }

    public void selectImage(android.support.v4.app.Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(fragment.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                mWeakReference = new WeakReference<>(fragment);
                fragment.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_REQUEST_CODE);

            } else {
                doSelect(fragment);
            }
        } else {
            doSelect(fragment);
        }
    }

    public void selectorImage(Activity activity) {
        if (Build.VERSION.SDK_INT >= 16) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //申请WRITE_EXTERNAL_STORAGE权限
                mWeakReference = new WeakReference<>(activity);
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_REQUEST_CODE);

            } else {
                doSelect(activity);
            }
        } else {
            doSelect(activity);
        }
    }

    private void doSelect(Activity activity) {
        Intent intent = createIntent();
        activity.startActivityForResult(intent, SELECT_PIC);
    }

    private void doSelect(android.support.v4.app.Fragment fragment) {
        Intent intent = createIntent();
        fragment.startActivityForResult(intent, SELECT_PIC);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void doSelect(android.app.Fragment fragment) {
        Intent intent = createIntent();
        fragment.startActivityForResult(intent, SELECT_PIC);
    }

    private Intent createIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);//ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/jpeg");
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
                mCallback.onSuccess(path);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mWeakReference != null) {
                    Object holder = mWeakReference.get();
                    if (holder != null) {
                        if (holder instanceof android.app.Fragment) {
                            doSelect((android.app.Fragment) holder);
                            return;
                        } else if (holder instanceof android.support.v4.app.Fragment) {
                            doSelect((android.support.v4.app.Fragment) holder);
                            return;
                        } else if (holder instanceof Activity) {
                            doSelect((Activity) holder);
                            return;
                        }
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
