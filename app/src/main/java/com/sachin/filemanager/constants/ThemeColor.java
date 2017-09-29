package com.sachin.filemanager.constants;

import com.sachin.filemanager.R;

public enum ThemeColor {

    RED(R.color.md_red_500, R.color.md_red_700, "Red"),
    PINK(R.color.md_pink_500, R.color.md_pink_700, "Pink"),
    PURPLE(R.color.md_purple_500, R.color.md_purple_700, "Purple"),
    DEEP_PURPLE(R.color.md_deep_purple_500, R.color.md_deep_purple_700, "Deep Purple"),
    INDIGO(R.color.md_indigo_500, R.color.md_indigo_700, "Indigo"),
    BLUE(R.color.md_blue_500, R.color.md_blue_700, "Blue"),
    LIGHT_BLUE(R.color.md_light_blue_500, R.color.md_light_blue_700, "Light Blue"),
    CYAN(R.color.md_cyan_500, R.color.md_cyan_700, "Cyan"),
    TEAL(R.color.md_teal_500, R.color.md_teal_700, "Teal"),
    GREEN(R.color.md_green_500, R.color.md_green_700, "Green"),
    LIGHT_GREEN(R.color.md_light_green_500, R.color.md_light_green_700, "Light Green"),
    LIME(R.color.md_lime_500, R.color.md_lime_700, "Lime"),
    YELLOW(R.color.md_yellow_500, R.color.md_yellow_700, "Yellow"),
    AMBER(R.color.md_amber_500, R.color.md_amber_700, "Amber"),
    ORANGE(R.color.md_orange_500, R.color.md_orange_700, "Orange"),
    DEEP_ORANGE(R.color.md_deep_orange_500, R.color.md_deep_orange_700, "Deep Orange"),
    BROWN(R.color.md_brown_500, R.color.md_brown_700, "Brown"),
    GREY(R.color.md_grey_500, R.color.md_grey_700, "Grey"),
    BLUE_GREY(R.color.md_blue_grey_500, R.color.md_blue_grey_700, "Blue Grey"),
    BLACK(R.color.md_black_1000, R.color.md_black_1000, "Black"),
    WHITE(R.color.md_white_1000, R.color.md_white_1000, "White"),
    DEFAULT_THEME(R.color.md_grey_50, R.color.md_grey_100, "Default Grey");

    private int colorResPrimary;
    private int colorResPrimaryDark;
    private String colorName;

    ThemeColor(int colorResPrimary, int colorResPrimaryDark, String colorName) {
        this.colorResPrimary = colorResPrimary;
        this.colorResPrimaryDark = colorResPrimaryDark;
        this.colorName = colorName;
    }

    public int getColorResPrimary() {
        return colorResPrimary;
    }

    public int getColorResPrimaryDark() {
        return colorResPrimaryDark;
    }

    public String getColorName() {
        return colorName;
    }
}
