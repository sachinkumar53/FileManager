package com.sachin.filemanager.ui;

import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.sachin.filemanager.FileManagerApplication;
import com.sachin.filemanager.constants.ThemeColor;
import com.sachin.filemanager.utils.SettingsUtils;

import static com.sachin.filemanager.constants.KEYS.PREFS_THEME_STRING;

public class ThemeUtils {
    private ThemeUtils() {
    }

    private static final String regex = ":";
    private static ThemeUtils instance = null;

    private String themeString;
    private ThemeColor themeColorPrimary;
    private ThemeColor themeColorAccent;
    private int baseTheme;
    private Theme theme;

    public static synchronized ThemeUtils getInstance() {
        if (instance == null)
            instance = new ThemeUtils();

        return instance;
    }

    public void init() {
        themeString = SettingsUtils.getString(PREFS_THEME_STRING, getDefaultThemeString());
        decodeThemeString(themeString);
        Log.w(getClass().getSimpleName(), themeString);
        theme = new Theme(themeColorPrimary, themeColorAccent, baseTheme);
    }

    public void decodeThemeString(String themeString) {
        String[] colors = themeString.split(regex);
        themeColorPrimary = ThemeColor.values()[Integer.parseInt(colors[0])];
        themeColorAccent = ThemeColor.values()[Integer.parseInt(colors[1])];
        baseTheme = Integer.parseInt(colors[2]);
    }

    public String generateThemeString() {
        String string = themeColorPrimary.ordinal() + regex + themeColorAccent.ordinal() + regex + String.valueOf(baseTheme);
        return string;
    }

    public String generateThemeString(ThemeColor themeColorPrimary, ThemeColor themeColorAccent, int base) {
        String string = themeColorPrimary.ordinal() + regex + themeColorAccent.ordinal() + regex + String.valueOf(base);
        return string;
    }

    public void setThemeColorPrimary(ThemeColor themeColorPrimary) {
        this.themeColorPrimary = themeColorPrimary;
    }

    public void setThemeColorAccent(ThemeColor themeColorAccent) {
        this.themeColorAccent = themeColorAccent;
    }

    public ThemeColor getThemeColorPrimary() {
        return themeColorPrimary;
    }

    public ThemeColor getThemeColorAccent() {
        return themeColorAccent;
    }

    public void setThemeString(String themeString) {
        this.themeString = themeString;
    }

    public String getDefaultThemeString() {
        themeString = generateThemeString(ThemeColor.DEFAULT_THEME, ThemeColor.GREEN, Theme.LIGHT);
        return themeString;
    }

    public String getThemeString() {
        return themeString;
    }


    public void setBaseTheme(int baseTheme) {
        this.baseTheme = baseTheme;
    }

    public int getBaseTheme() {
        return baseTheme;
    }

    public Theme getTheme() {
        return theme;
    }

    public String applyChanges() {
        themeString = generateThemeString();
        theme = new Theme(themeColorPrimary, themeColorAccent, baseTheme);
        SettingsUtils.applySettings(PREFS_THEME_STRING, themeString);
        return themeString;
    }

    public static int getColorFromThemeColor(ThemeColor themeColor) {
        return ContextCompat.getColor(FileManagerApplication.getAppContext(), themeColor.getColorResPrimary());
    }
}
