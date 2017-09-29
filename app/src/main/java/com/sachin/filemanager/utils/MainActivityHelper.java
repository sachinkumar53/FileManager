package com.sachin.filemanager.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.sachin.filemanager.FileManager;
import com.sachin.filemanager.R;
import com.sachin.filemanager.activities.MainActivity;
import com.sachin.filemanager.adapters.MainAdapter;
import com.sachin.filemanager.constants.KEYS;
import com.sachin.filemanager.fragments.MainDialogFragment;
import com.sachin.filemanager.layouts.MyGridLayoutManager;
import com.sachin.filemanager.layouts.MyLinearLayoutManager;
import com.sachin.filemanager.ui.FileItem;
import com.sachin.filemanager.view.HideShowScrollListener;

import java.lang.reflect.Field;
import java.util.List;


public class MainActivityHelper implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener, KEYS {
    public FileManager fileManager;
    boolean fabOpened = false;
    private MainActivity mainActivity;
    private RecyclerView recyclerView;
    private MainAdapter mainAdapter;
    private FileManagerUtils fileManagerUtils;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fab;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private View fabContainer;
    private View fabLayout1;
    private View fabLayout2;
    private Animation rC;
    private Animation rAC;
    private Animation show;
    private Animation hide;
    private IconLoader iconLoader;

    public MainActivityHelper(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        fileManagerUtils = FileManagerUtils.getInstance();
        //this.fileManager = mainActivity.fileManager;
        this.recyclerView = mainActivity.recyclerView;
        this.mainAdapter = mainActivity.mainAdapter;

        this.bottomNavigationView = mainActivity.bottomNavigationView;
        disableShiftAnimation();

        fab = mainActivity.fab;
        fab1 = mainActivity.fab1;
        fab2 = mainActivity.fab2;

        fabContainer = mainActivity.fabContainer;
        fabLayout1 = mainActivity.fabLayout1;
        fabLayout2 = mainActivity.fabLayout2;

        rC = AnimationUtils.loadAnimation(getContext(), R.anim.fab_rotate_cw);
        rAC = AnimationUtils.loadAnimation(getContext(), R.anim.fab_rotate_acw);
        show = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        hide = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
        hide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isFabOpened()) {
                    fabLayout1.setVisibility(View.INVISIBLE);
                    fabLayout2.setVisibility(View.INVISIBLE);
                    fabOpened = false;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        recyclerView.addOnScrollListener(new HideShowScrollListener(fab));
    }

        public void updateDirectory(List<FileItem> nextDir) {
        mainAdapter.stopThumbnailThread();
        mainAdapter.addAll(nextDir);
        mainAdapter.refresh();

        if (isFabOpened()) {
            hideFAB();
        }
    }
    public void startIconLoading(List<FileItem> itemList){
        if (iconLoader == null)
            iconLoader = new IconLoader();

        for (FileItem item:itemList){
            if (item.isFile()){
                if (iconLoader.hasLoadedCache(item.getPath()) == null){

//                    iconLoader.loadIcon(itemList,);
                }
            }
        }
    }

    public void showDialog(MainDialogFragment dialogFragment) {
        dialogFragment.show(mainActivity.getSupportFragmentManager(), "tag");
    }

    public void switchLayout(int viewType) {
        if (isFabOpened())
            hideFAB();

        mainAdapter.setViewType(viewType);
        if (viewType == MainAdapter.LIST_VIEW)
            recyclerView.setLayoutManager(new MyLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false, mainAdapter));
        else
            recyclerView.setLayoutManager(new MyGridLayoutManager(getContext(),
                    getContext().getResources().getInteger(R.integer.column_count), mainAdapter));

        recyclerView.setAdapter(mainAdapter);
    }

    public MainAdapter getMainAdapter() {
        return mainAdapter;
    }


    public Uri getTreeUri() {
        Uri treeUri = SettingsUtils.getTreeUri();
        return treeUri;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public Context getContext() {
        return mainActivity.getBaseContext();
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab_main) {
            if (fabOpened) {
                hideFAB();
            } else {
                showFAB();
            }

        } else if (v.getId() == R.id.fab_create_folder) {
            if (getTreeUri() == null) {
                showDialog(MainDialogFragment.newInstance(MainDialogFragment.SAF_TYPE_DIALOG));
                return;
            }
            MainDialogFragment dialogFragment = MainDialogFragment.newInstance(MainDialogFragment.CREATE_FOLDER_DIALOG);
            hideFAB();
            showDialog(dialogFragment);
        } else if (v.getId() == R.id.fab_container) {
            if (isFabOpened()) {
                hideFAB();
            }
        }
    }

    public boolean isFabOpened() {
        return fabOpened;
    }

    private void showFAB() {
        fab.clearAnimation();
        fab.startAnimation(rC);

        fab1.clearAnimation();
        fab2.clearAnimation();

        fab1.startAnimation(show);
        fab2.startAnimation(show);

        fabLayout1.setVisibility(View.VISIBLE);
        fabLayout2.setVisibility(View.VISIBLE);

        fab1.setClickable(true);
        fab2.setClickable(true);
        fabContainer.setClickable(true);
        fabContainer.setBackgroundColor(Color.parseColor("#30000000"));

        if (!isFabOpened())
            fabOpened = true;
    }

    public void hideFAB() {

        fab1.setClickable(false);
        fab2.setClickable(false);

        fab1.clearAnimation();
        fab2.clearAnimation();

        fab1.startAnimation(hide);
        fab2.startAnimation(hide);

        fabContainer.setClickable(false);
        fabContainer.setBackgroundColor(Color.TRANSPARENT);

        fab.clearAnimation();
        fab.startAnimation(rAC);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }

    public void updateSortSettings(int sortType) {
        fileManagerUtils.setSortType(sortType);
        updateDirectory(fileManagerUtils.getNextDirectory(fileManagerUtils.getCurrentDirectory(), true));
        SettingsUtils.applySettings(PREFS_SORT, sortType);
    }

    public String getPathLabel(String directory) {
        String pathLabel = null;
        if (directory.equals(StorageUtils.getSdCardPath()))
            pathLabel = "DEVICE STORAGE";

        else if (directory.equals(StorageUtils.getExtSdCardPaths(mainActivity.getBaseContext())))
            pathLabel = "SD CARD";

        else if (directory.equals(StorageUtils.getRootPath()))
            pathLabel = "ROOT";

        return pathLabel;
    }

    public void disableShiftAnimation() {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);

            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(i);
                itemView.setShiftingMode(false);
                itemView.setChecked(itemView.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e(getClass().getSimpleName(), "unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e(getClass().getSimpleName(), "unable to change value of shift mode", e);
        }
    }
}
