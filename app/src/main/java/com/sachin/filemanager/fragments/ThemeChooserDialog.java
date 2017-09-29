package com.sachin.filemanager.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import com.sachin.filemanager.R;
import com.sachin.filemanager.adapters.ColorGridAdapter;
import com.sachin.filemanager.adapters.ThemePagerAdapter;
import com.sachin.filemanager.constants.ThemeColor;
import com.sachin.filemanager.ui.Icons;
import com.sachin.filemanager.ui.Theme;
import com.sachin.filemanager.ui.ThemeUtils;
import com.sachin.filemanager.utils.IconUtils;

public class ThemeChooserDialog extends DialogFragment implements DialogInterface.OnClickListener,
        RadioGroup.OnCheckedChangeListener,ColorGridAdapter.OnItemClickListener {
    private static ThemeChooserDialog instance;

    private int[] radioButtonsId = {R.id.theme_light,R.id.theme_dark,R.id.theme_auto};
    private ViewPager viewPager;
    private ThemePagerAdapter pagerAdapter;
    private RadioGroup radioGroup;
    private int currentColor;
    private ThemeUtils themeUtils;

    public static ThemeChooserDialog getInstance() {
        if (instance == null) {
            instance = new ThemeChooserDialog();
        }
        return instance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setNegativeButton(getActivity().getString(android.R.string.cancel), this);
        builder.setPositiveButton(getActivity().getString(android.R.string.ok), this);

        themeUtils = ThemeUtils.getInstance();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.theme_chooser_dialog, null, false);
        builder.setView(view);

        viewPager = (ViewPager) view.findViewById(R.id.theme_dialog_pager);
        TabLayout tabLayout = (TabLayout)view.findViewById(R.id.color_tab);
        tabLayout.setVisibility(View.GONE);
        tabLayout.setupWithViewPager(viewPager);

        pagerAdapter = new ThemePagerAdapter(getActivity());
        pagerAdapter.setOnItemClickListener(this);
        currentColor = themeUtils.getTheme().getThemeColorAccent().ordinal();
        pagerAdapter.setColorSelected(currentColor);

        viewPager.setAdapter(pagerAdapter);

        radioGroup = (RadioGroup) view.findViewById(R.id.radio_group);
        radioGroup.check(radioButtonsId[themeUtils.getTheme().getBaseTheme()]);
        radioGroup.setOnCheckedChangeListener(this);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == AlertDialog.BUTTON_POSITIVE) {
            themeUtils.generateThemeString();
            themeUtils.applyChanges();
            Icons.destroyIconCache();
            getActivity().recreate();
            dismiss();
        } else
            dismiss();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        int checked = group.getCheckedRadioButtonId();
        switch (checked){
            case R.id.theme_dark:
                themeUtils.setBaseTheme(Theme.DARK);
                break;
            case R.id.theme_light:
                themeUtils.setBaseTheme(Theme.LIGHT);
                break;
            case R.id.theme_auto:
                themeUtils.setBaseTheme(Theme.AUTO);
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        pagerAdapter.setColorSelected(position);
        themeUtils.setThemeColorAccent(ThemeColor.values()[position]);
    }
}
