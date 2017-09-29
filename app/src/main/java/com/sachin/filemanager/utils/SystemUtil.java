package com.sachin.filemanager.utils;

import android.os.Build;

public class SystemUtil {

    public static boolean isKitkat() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT;
    }


    public static boolean isAndroid5() {
        return isAtLeastVersion(Build.VERSION_CODES.LOLLIPOP);
    }

    public static boolean isAtLeastVersion(final int version) {
        return Build.VERSION.SDK_INT >= version;
    }


}
