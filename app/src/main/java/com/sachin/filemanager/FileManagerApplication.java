package com.sachin.filemanager;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.sachin.filemanager.ui.ThemeUtils;

public class FileManagerApplication extends Application {
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        ThemeUtils themeUtils = ThemeUtils.getInstance();
        themeUtils.init();
        Log.w(getClass().getSimpleName(), "Created");
    }

    public static Context getAppContext() {
        return appContext;
    }
}
