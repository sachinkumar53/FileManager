package com.sachin.filemanager.ui;

import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.sachin.filemanager.FileManagerApplication;
import com.sachin.filemanager.constants.ThemeColor;
import com.sachin.filemanager.utils.SettingsUtils;

import static com.sachin.filemanager.constants.KEYS.PREFS_THEME_STRING;

public class ThemeUtils {
    private static final String regex = ":";
    private static ThemeUtils instance = null;
    private String themeString;
    private ThemeColor themeColorPrimary;
    private ThemeColor themeColorAccent;
    private int baseTheme;
    private boolean fullBlack;
    private Theme theme;
    private ThemeUtils() {
    }

    public static synchronized ThemeUtils getInstance() {
        if (instance == null)
            instance = new ThemeUtils();

        return instance;
    }

    public static int getColorFromThemeColor(ThemeColor themeColor) {
        return ContextCompat.getColor(FileManagerApplication.getAppContext(), themeColor.getColorResPrimary());
    }

    public void init() {
        themeString = SettingsUtils.getString(PREFS_THEME_STRING, getDefaultThemeString());
        decodeThemeString(themeString);
        Log.w(getClass().getSimpleName(), themeString);
        theme = new Theme(themeColorPrimary, themeColorAccent, baseTheme, fullBlack);
    }

    public void decodeThemeString(String themeString) {
        String[] colors = themeString.split(regex);
        themeColorPrimary = ThemeColor.values()[Integer.parseInt(colors[0])];
        themeColorAccent = ThemeColor.values()[Integer.parseInt(colors[1])];
        baseTheme = Integer.parseInt(colors[2]);
        fullBlack = Boolean.parseBoolean(colors[3]);
    }

    public String generateThemeString() {
        String string = themeColorPrimary.ordinal() + regex + themeColorAccent.ordinal() + regex + baseTheme + regex + fullBlack;
        return string;
    }

    public String generateThemeString(ThemeColor themeColorPrimary, ThemeColor themeColorAccent, int base, boolean fullBlack) {
        String string = themeColorPrimary.ordinal() + regex + themeColorAccent.ordinal() + regex + base + regex + fullBlack;
        return string;
    }

    public void setFullBlack(boolean fullBlack) {
        this.fullBlack = fullBlack;
    }

    public ThemeColor getThemeColorPrimary() {
        return themeColorPrimary;
    }

    public void setThemeColorPrimary(ThemeColor themeColorPrimary) {
        this.themeColorPrimary = themeColorPrimary;
    }

    public ThemeColor getThemeColorAccent() {
        return themeColorAccent;
    }

    public void setThemeColorAccent(ThemeColor themeColorAccent) {
        this.themeColorAccent = themeColorAccent;
    }

    public String getDefaultThemeString() {
        themeString = generateThemeString(ThemeColor.DEFAULT_THEME, ThemeColor.GREEN, Theme.LIGHT, false);
        return themeString;
    }

    public String getThemeString() {
        return themeString;
    }

    public void setThemeString(String themeString) {
        this.themeString = themeString;
    }

    public int getBaseTheme() {
        return baseTheme;
    }

    public void setBaseTheme(int baseTheme) {
        this.baseTheme = baseTheme;
    }

    public Theme getTheme() {
        return theme;
    }

    public String applyChanges() {
        themeString = generateThemeString();
        theme = new Theme(themeColorPrimary, themeColorAccent, baseTheme, fullBlack);
        SettingsUtils.applySettings(PREFS_THEME_STRING, themeString);
        return themeString;
    }
}
