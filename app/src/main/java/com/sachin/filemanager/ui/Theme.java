package com.sachin.filemanager.ui;

import android.support.v4.content.ContextCompat;

import com.sachin.filemanager.FileManagerApplication;
import com.sachin.filemanager.R;
import com.sachin.filemanager.constants.ThemeColor;

public class Theme {
    public static final int LIGHT = 0;
    public static final int DARK = 1;
    public static final int AUTO = 2;

    private int[] baseThemesRes = {R.style.AppTheme_Light, R.style.AppTheme_Dark, R.style.AppTheme_Auto};
    private int baseTheme = LIGHT;
    private ThemeColor themeColorPrimary;
    private ThemeColor themeColorAccent;

    private int styleResPrimary;
    private int styleResAccent;
    private int styleResBase;
    private boolean useBlack;

    public Theme(ThemeColor themeColorPrimary, ThemeColor themeColorAccent, int baseTheme,boolean fullBlack) {
        this.themeColorPrimary = themeColorPrimary;
        this.themeColorAccent = themeColorAccent;
        this.baseTheme = baseTheme;
        this.useBlack = fullBlack;

        int primary = themeColorPrimary.ordinal();
        int accent = themeColorAccent.ordinal();

        styleResPrimary = FileManagerApplication.getAppContext().getResources().getIdentifier("primary" + primary, "style",
                FileManagerApplication.getAppContext().getPackageName());

        styleResAccent = FileManagerApplication.getAppContext().getResources().getIdentifier("accent" + accent, "style",
                FileManagerApplication.getAppContext().getPackageName());

        try {
            styleResBase = baseThemesRes[baseTheme];

            if (!fullBlack || baseTheme != DARK)
                return;

                styleResBase = R.style.AppTheme_Dark_Black;

        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public ThemeColor getThemeColorPrimary() {
        return themeColorPrimary;
    }

    public ThemeColor getThemeColorAccent() {
        return themeColorAccent;
    }

    public int getColorPrimary() {
        int color = ContextCompat.getColor(FileManagerApplication.getAppContext(),
                ThemeColor.values()[themeColorPrimary.ordinal()].getColorResPrimary());
        return color;
    }

    public int getColorAccent() {
        int color = ContextCompat.getColor(FileManagerApplication.getAppContext(),
                ThemeColor.values()[themeColorAccent.ordinal()].getColorResPrimary());
        return color;
    }

    public int getBaseTheme() {
        return baseTheme;
    }

    public int getStyleResAccent() {
        return styleResAccent;
    }

    public int getStyleResBase() {
        return styleResBase;
    }

    public int getStyleResPrimary() {
        return styleResPrimary;
    }

    public boolean isFullBlack() {
        return useBlack;
    }
}
