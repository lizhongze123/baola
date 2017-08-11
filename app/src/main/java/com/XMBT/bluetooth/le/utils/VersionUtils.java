package com.XMBT.bluetooth.le.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by lzz on 2017/8/10.
 */

public class VersionUtils {

    public static PackageInfo getPackageInfo(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    public static String getAppVersionName(Context context) {
        return "v" + getPackageInfo(context).versionName;
    }
}
