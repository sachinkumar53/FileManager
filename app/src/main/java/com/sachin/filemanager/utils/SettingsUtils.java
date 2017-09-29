package com.sachin.filemanager.utils;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.sachin.filemanager.FileManagerApplication;
import com.sachin.filemanager.constants.KEYS;

public class SettingsUtils implements KEYS {

    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;

    private SettingsUtils() {
        throw new UnsupportedOperationException();
    }

    public static SharedPreferences getPreferences() {
        if (preferences == null)
            preferences = PreferenceManager.getDefaultSharedPreferences(FileManagerApplication.getAppContext());

        return preferences;
    }

    public static void applySettings(String key, String value) {
        if (editor == null)
            editor = getPreferences().edit();

        editor.putString(key, value);
        editor.commit();
    }

    public static void applySettings(String key, boolean value) {
        if (editor == null)
            editor = getPreferences().edit();

        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void applySettings(String key, int value) {
        if (editor == null)
            editor = getPreferences().edit();

        editor.putInt(key, value);
        editor.commit();
    }


    public static String getString(String key, String defValue) {
        String setting = getPreferences().getString(key, defValue);
        return setting;
    }

    public static int getInt(String key, int defValue) {
        int setting = getPreferences().getInt(key, defValue);
        return setting;
    }

    public static boolean getBoolean(String key, boolean defValue) {
        boolean setting = getPreferences().getBoolean(key, defValue);
        return setting;
    }

    public static boolean isThumbnailEnabled(){
        return getBoolean(PREFS_THUMBS,true);
    }

    public static Uri getSettingsUri(String id) {
        String uri = getPreferences().getString(id, null);

        if (uri == null)
            return null;

        return Uri.parse(uri);
    }

    public static Uri getTreeUri() {
        Uri uri = getSettingsUri(PREFS_TREE_URI);
        return uri;
    }
}
