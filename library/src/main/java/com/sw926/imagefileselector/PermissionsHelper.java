package com.sw926.imagefileselector;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

/**
 * Created by sunwei on 7/1/16.
 *
 */
public class PermissionsHelper {


    public interface Callback {
        void onRequestPermissionsCallback(boolean isGranted);
    }

    @Nullable
    private Callback mCurrentCallback;
    @Nullable
    private String[] mCurrentRequestPermissions;

    private int mCurrentRequestCode;

    @SuppressWarnings("WeakerAccess")
    public void checkAndRequestPermission(@NonNull Activity activity, int requestCode, @NonNull Callback callback, @NonNull String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isHavePermissions(activity)) {
                callback.onRequestPermissionsCallback(true);
                setCurrentRequest(null, null, -1);
            } else {
                setCurrentRequest(callback, permissions, requestCode);
                activity.requestPermissions(permissions, requestCode);
            }
        } else {
            callback.onRequestPermissionsCallback(true);
            setCurrentRequest(null, null, -1);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void checkAndRequestPermission(@NonNull Fragment fragment, int requestCode, @NonNull Callback callback, @NonNull String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isHavePermissions(fragment.getContext())) {
                callback.onRequestPermissionsCallback(true);
                setCurrentRequest(null, null, -1);
            } else {
                setCurrentRequest(callback, permissions, requestCode);
                fragment.requestPermissions(permissions, requestCode);
            }
        } else {
            callback.onRequestPermissionsCallback(true);
            setCurrentRequest(null, null, -1);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean isAllGranted = true;
        if (requestCode == mCurrentRequestCode){
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
        }

        if (mCurrentCallback != null) {
            mCurrentCallback.onRequestPermissionsCallback(isAllGranted);
        }
    }

    private void setCurrentRequest(Callback currentCallback, String[] currentRequestPermissions, int requestCode) {
        mCurrentCallback = currentCallback;
        mCurrentRequestPermissions = currentRequestPermissions;
        mCurrentRequestCode = requestCode;
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean isHavePermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
