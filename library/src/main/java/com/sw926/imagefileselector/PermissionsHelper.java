package com.sw926.imagefileselector;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

/**
 * Created by sunwei on 7/1/16.
 */
class PermissionsHelper {

    public static boolean checkAndRequestPermission(Object object, String permission, int requestCode) {
        if (object != null) {
            if (object instanceof Activity) {
                return checkAndRequestPermission((Activity) object, permission, requestCode);
            } else if (object instanceof Fragment) {
                return checkAndRequestPermission((Fragment) object, permission, requestCode);
            } else if (object instanceof android.app.Fragment) {
                return checkAndRequestPermission((android.app.Fragment) object, permission, requestCode);
            }
        }
        return false;
    }

    public static boolean checkAndRequestPermission(Activity activity, String permission, int requestCode) {
        if (isHavePermission(activity)) {
            return true;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(new String[]{permission}, requestCode);
            }
            return false;
        }
    }

    public static boolean checkAndRequestPermission(Fragment fragment, String permission, int requestCode) {
        if (isHavePermission(fragment.getContext())) {
            return true;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                fragment.requestPermissions(new String[]{permission}, requestCode);
            }
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static boolean checkAndRequestPermission(android.app.Fragment fragment, String permission, int requestCode) {
        if (isHavePermission(fragment.getActivity())) {
            return true;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                fragment.requestPermissions(new String[]{permission}, requestCode);
            }
            return false;
        }
    }

    private static boolean isHavePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
