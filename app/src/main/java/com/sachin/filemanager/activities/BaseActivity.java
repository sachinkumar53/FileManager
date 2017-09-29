package com.sachin.filemanager.activities;

import android.app.ActivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;

import com.sachin.filemanager.ui.ThemeUtils;

public class BaseActivity extends AppCompatActivity {
    private String themeString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        themeString = ThemeUtils.getInstance().getThemeString();

        setTheme(ThemeUtils.getInstance().getTheme().getStyleResBase());

        getTheme().applyStyle(ThemeUtils.getInstance().getTheme().getStyleResPrimary(), true);
        getTheme().applyStyle(ThemeUtils.getInstance().getTheme().getStyleResAccent(), true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager.TaskDescription tDesc = new ActivityManager.TaskDescription(null, null,
                    ContextCompat.getColor(this,ThemeUtils.getInstance().getTheme().getThemeColorPrimary().getColorResPrimary()));
            setTaskDescription(tDesc);
        }

        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ThemeUtils.getInstance().getThemeString().equals(themeString)) {
            Log.d(getClass().getSimpleName(), "Theme change detected, restarting activity");
            recreate();
        }
    }
}
