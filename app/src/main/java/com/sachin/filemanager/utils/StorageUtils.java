package com.sachin.filemanager.utils;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class StorageUtils {

    private static final String TAG = "StorageUtils";

    private static final String PRIMARY_VOLUME_NAME = "primary";
    private static final String SECONDARY_VOLUME_NAME = "external";

    private static final String ROOT = "Root";
    private static final String INTERNAL = "Device Storage";
    private static final String EXTERNAL = "SD Card";

    @NonNull
    public static String getSdCardPath() {
        String sdCardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        try {
            sdCardDirectory = new File(sdCardDirectory).getCanonicalPath();
        } catch (IOException ioe) {
            Log.e(TAG, "Could not get SD directory", ioe);
        }
        return sdCardDirectory;
    }

    @NonNull
    public static String getRootPath() {
        String sdCardDirectory = Environment.getRootDirectory().getParent();
        try {
            sdCardDirectory = new File(sdCardDirectory).getCanonicalPath();
        } catch (IOException ioe) {
            Log.e(TAG, "Could not get SD directory", ioe);
        }
        return sdCardDirectory;
    }


    public static String getExtSdCardPaths(Context con) {
        ArrayList<String> paths = new ArrayList<String>();
        File[] files = ContextCompat.getExternalFilesDirs(con, SECONDARY_VOLUME_NAME);
        File firstFile = files[0];
        for (File file : files) {
            if (file != null && !file.equals(firstFile)) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        return paths.get(0);
    }
}