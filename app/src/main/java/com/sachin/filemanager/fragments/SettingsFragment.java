package com.sachin.filemanager.fragments;

import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.sachin.filemanager.R;
import com.sachin.filemanager.constants.KEYS;
import com.sachin.filemanager.ui.ColorPreference;
import com.sachin.filemanager.utils.SettingsUtil;

public class SettingsFragment extends PreferenceFragmentCompat implements OnPreferenceChangeListener, KEYS {

    private CheckBoxPreference hiddenFiles;
    private CheckBoxPreference remPath;
    private CheckBoxPreference thumbNails;
    private SwitchPreferenceCompat rootMode;
    private ColorPreference colorPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        addPreferencesFromResource(R.xml.file_manager_settings);

        hiddenFiles = (CheckBoxPreference) findPreference(PREFS_HIDDEN);
        remPath = (CheckBoxPreference) findPreference(PREFS_REM_PATH);
        thumbNails = (CheckBoxPreference) findPreference(PREFS_THUMBS);
        rootMode = (SwitchPreferenceCompat) findPreference(PREFS_ROOT_MODE);

        hiddenFiles.setOnPreferenceChangeListener(this);
        remPath.setOnPreferenceChangeListener(this);
        thumbNails.setOnPreferenceChangeListener(this);
        rootMode.setOnPreferenceChangeListener(this);

        colorPreference = (ColorPreference) findPreference(getContext().getString(R.string.key_theme_style));
        colorPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ThemeChooserDialog dialog = ThemeChooserDialog.getInstance();
                dialog.show(getFragmentManager(), "tag");
                return true;
            }
        });
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference instanceof CheckBoxPreference) {
            if (preference.equals(hiddenFiles)) {
                SettingsUtil.applySettings(PREFS_HIDDEN, Boolean.parseBoolean(String.valueOf(newValue)));
                return true;
            } else if (preference.equals(remPath)) {
                SettingsUtil.applySettings(PREFS_REM_PATH, Boolean.parseBoolean(String.valueOf(newValue)));
                return true;
            } else if (preference.equals(thumbNails)) {
                SettingsUtil.applySettings(PREFS_THUMBS, Boolean.parseBoolean(String.valueOf(newValue)));
                return true;
            }
        } else if (preference instanceof SwitchPreferenceCompat) {
            SettingsUtil.applySettings(PREFS_ROOT_MODE, Boolean.parseBoolean(String.valueOf(newValue)));
            return true;

        }
        return false;
    }

}
