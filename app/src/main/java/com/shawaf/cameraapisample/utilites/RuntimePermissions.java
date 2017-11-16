package com.shawaf.cameraapisample.utilites;

import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

/**
 * Created by Shawaf on 1/9/2017.
 */

public class RuntimePermissions {

    public static boolean shouldShowRequestPermissionRationale(String[] permissions, Activity activity) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }


    public static void askForCameraPermisionsInActivity(Activity activity, String[] CAM_PERMISSIONS, int CAM_ASK_PERMISSION_CODE) {
        if (shouldAskPermission()) {
            ActivityCompat.requestPermissions(activity, CAM_PERMISSIONS, CAM_ASK_PERMISSION_CODE);
        }
    }

    //Check the version of sdk is installed
    public static boolean shouldAskPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }
}
