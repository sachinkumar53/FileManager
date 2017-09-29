package com.sachin.filemanager.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.sachin.filemanager.R;
import com.sachin.filemanager.fragments.SettingsFragment;

public class SettingsActivity extends BaseActivity {
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, new SettingsFragment()).commit();
    }
}
