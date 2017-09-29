package com.sachin.filemanager.ui;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.sachin.filemanager.R;
import com.sachin.filemanager.constants.ThemeColor;

public class ColorPreference extends Preference {
    private ThemeUtils themeUtils;
    private ImageView colorView;
    private ShapeDrawable drawable;
    private ThemeColor themeColor;

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWidgetLayoutResource(R.layout.preference_color);
        themeUtils = ThemeUtils.getInstance();
        themeColor = themeUtils.getTheme().getThemeColorAccent();
        setSummary(themeColor.getColorName());
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        drawable = new ShapeDrawable(new OvalShape());
        drawable.getPaint().setStyle(Paint.Style.FILL);
        drawable.getPaint().setColor(getColor(themeColor));
        colorView = (ImageView) holder.findViewById(R.id.color_view);
        colorView.setBackground(drawable);
    }

    private int getColor(ThemeColor themeColor) {
        return ContextCompat.getColor(getContext(), themeColor.getColorResPrimary());
    }
}
