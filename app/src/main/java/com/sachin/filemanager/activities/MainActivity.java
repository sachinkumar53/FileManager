package com.sachin.filemanager.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.sachin.filemanager.R;
import com.sachin.filemanager.adapters.MainAdapter;
import com.sachin.filemanager.constants.KEYS;
import com.sachin.filemanager.fragments.MainDialogFragment;
import com.sachin.filemanager.ui.DragSelectTouchListener;
import com.sachin.filemanager.ui.FileItem;
import com.sachin.filemanager.ui.IconHolder;
import com.sachin.filemanager.utils.AnimationUtils;
import com.sachin.filemanager.utils.FileListSorter;
import com.sachin.filemanager.utils.FileManagerUtils;
import com.sachin.filemanager.utils.FileUtils;
import com.sachin.filemanager.utils.IconLoader;
import com.sachin.filemanager.utils.MainActivityHelper;
import com.sachin.filemanager.utils.SettingsUtils;
import com.sachin.filemanager.utils.StorageUtils;
import com.sachin.filemanager.utils.SystemUtil;
import com.sachin.filemanager.view.PathLabelView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity implements KEYS,
        MainAdapter.OnItemClickListener, MainAdapter.OnItemLongClickListener, ActionMode.Callback,
        MainAdapter.OnOptionItemClickListener, NavigationView.OnNavigationItemSelectedListener, MainAdapter.OnSelectionChanged
        , DragSelectTouchListener.OnDragSelectListener {

    public final String TAG = getClass().getSimpleName();

    private static final int REQUEST_CODE = 0x11;
    private MainActivityHelper helper;

    //Shared
    public FileManagerUtils managerUtils;
    public RecyclerView recyclerView;
    public MainAdapter mainAdapter;
    public BottomNavigationView bottomNavigationView;
    public FloatingActionButton fab;
    public FloatingActionButton fab1;
    public FloatingActionButton fab2;
    public View fabContainer;
    public View fabLayout1;
    public View fabLayout2;

    public DrawerLayout drawer;
    private Toolbar actionBar;
    private ActionMode actionMode;
    private String homeDirectory;
    private PathLabelView pathLabelView;
    private int sortType;
    private boolean showHidden;
    private boolean showTumbs;
    private boolean rememberPath;
    private boolean rootMode;
    private int viewType;
    private String treeUri;

    private DragSelectTouchListener dragSelectTouchListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        checkAndAskPermissions();

        initSettings();

        managerUtils = FileManagerUtils.getInstance();
        managerUtils.setSortType(sortType);
        managerUtils.setShowHidden(showHidden);

        recyclerView = (RecyclerView) findViewById(R.id.file_list);
        mainAdapter = new MainAdapter(this);

        List<FileItem> fileItems;

        if (savedInstanceState != null)
            fileItems = new ArrayList<>(managerUtils.getNextDirectory(savedInstanceState.getString("path"), true));
        else
            fileItems = new ArrayList<>(managerUtils.setHomeDirectory(getHomeDirectory()));

        mainAdapter.addAll(fileItems);

        init();
    }

    private void initSettings() {
        showHidden = SettingsUtils.getBoolean(PREFS_HIDDEN, false);
        rememberPath = SettingsUtils.getBoolean(PREFS_REM_PATH, true);
        sortType = SettingsUtils.getInt(PREFS_SORT, FileListSorter.SORT_BY_TYPE);
        showTumbs = SettingsUtils.getBoolean(PREFS_THUMBS, true);
        rootMode = SettingsUtils.getBoolean(PREFS_ROOT_MODE, false);
    }

    private void init() {
        actionBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(actionBar);

        drawer = (DrawerLayout) findViewById(R.id.main_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, actionBar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setUpNavigationDrawer(navigationView.getMenu());

        dragSelectTouchListener = new DragSelectTouchListener();
        dragSelectTouchListener.withDebug(false);
        dragSelectTouchListener.withSelectListener(this);

        pathLabelView = (PathLabelView) findViewById(R.id.path_label_view);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_tool_bar);
        fab = (FloatingActionButton) findViewById(R.id.fab_main);
        fab2 = (FloatingActionButton) findViewById(R.id.fab_create_file);
        fab1 = (FloatingActionButton) findViewById(R.id.fab_create_folder);

        fabContainer = findViewById(R.id.fab_container);
        fabLayout1 = findViewById(R.id.fab_layout_1);
        fabLayout2 = findViewById(R.id.fab_layout_2);

        helper = new MainActivityHelper(this);

        setUP();
        //updateUI();
    }

    private void setUP() {
        mainAdapter.setOnItemClickListener(this);
        mainAdapter.setOnItemLongClickListener(this);
        mainAdapter.setOnOptionItemClickListener(this);
        mainAdapter.setOnSelectionChanged(this);
        recyclerView.setHasFixedSize(true);
        helper.switchLayout(getViewType());
        pathLabelView.setHelper(helper);
        if (isHome())
            pathLabelView.updatePathLabel(managerUtils.getCurrentDirectory(), true, false);
        else {
            String s = managerUtils.getCurrentDirectory();
            String home = homeType(s);
            String remainSub = s.substring(home.length() + 1, s.length());
            String[] subs = remainSub.split("/");
            pathLabelView.updatePathLabel(home, true, false);
            for (String sub : subs)
                pathLabelView.updatePathLabel(sub, home + "/" + sub, false, true);
        }
        fab.setOnClickListener(helper);
        fab1.setOnClickListener(helper);
        fab2.setOnClickListener(helper);
        fabContainer.setOnClickListener(helper);
        fabContainer.setClickable(false);

        recyclerView.addOnItemTouchListener(dragSelectTouchListener);
    }

    private boolean isHome() {
        if (getHomeDirectory().equals(StorageUtils.getSdCardPath()) ||
                getHomeDirectory().equals(StorageUtils.getExtSdCardPaths(this)) ||
                getHomeDirectory().equals(StorageUtils.getRootPath()))
            return true;

        return false;
    }

    private String homeType(String main) {
        if (main.contains(StorageUtils.getSdCardPath()))
            return StorageUtils.getSdCardPath();

        else if (main.contains(StorageUtils.getExtSdCardPaths(this)))
            return StorageUtils.getExtSdCardPaths(this);

        else if (main.contains(StorageUtils.getRootPath()))
            return StorageUtils.getRootPath();
        else
            return StorageUtils.getSdCardPath();
    }

    private void checkAndAskPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    private void setUpNavigationDrawer(Menu menu) {
        MenuItem primary = menu.findItem(R.id.nav_device);
        MenuItem sdCard = menu.findItem(R.id.nav_sd_card);
        if (StorageUtils.getExtSdCardPaths(this) != null) {
            if (!sdCard.isVisible())
                sdCard.setVisible(true);
        } else {
            if (sdCard.isVisible())
                sdCard.setVisible(false);
        }

        MenuItem root = menu.findItem(R.id.nav_root);
        boolean device = getHomeDirectory().equals(StorageUtils.getSdCardPath());
        primary.setChecked(device);
        boolean sd = getHomeDirectory().equals(StorageUtils.getExtSdCardPaths(this));
        sdCard.setChecked(sd);
        root.setVisible(rootMode);

    }

    private int getViewType() {
        viewType = SettingsUtils.getInt(PREFS_VIEW_MODE, MainAdapter.LIST_VIEW);
        return viewType;
    }

    public void updateUI() {
        helper.updateDirectory(managerUtils.getNextDirectory(managerUtils.getCurrentDirectory(), true));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    updateUI();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("path", managerUtils.getCurrentDirectory());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.myfiles_options_menu, menu);

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);

        Drawable grid = ContextCompat.getDrawable(this, R.drawable.abc_ic_icon_grid);
        Drawable list = ContextCompat.getDrawable(this, R.drawable.abc_ic_icon_list);

        DrawableCompat.setTint(grid, ContextCompat.getColor(this, typedValue.resourceId));
        DrawableCompat.setTint(list, ContextCompat.getColor(this, typedValue.resourceId));

        menu.findItem(R.id.menu_id_search).setIcon(android.support.v7.appcompat.R.drawable.abc_ic_search_api_material);
        MenuItem item = menu.findItem(R.id.switchLayout);

        item.setIcon(mainAdapter.getViewType() == MainAdapter.LIST_VIEW ?
                grid : list);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_id_sort:
                MainDialogFragment dialogFragment = MainDialogFragment.newInstance(MainDialogFragment.SORT_TYPE_DIALOG);
                helper.showDialog(dialogFragment);
                return true;

            case R.id.menu_id_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_id_quit:
                finish();
                return true;
            case R.id.switchLayout:
                if (mainAdapter.getViewType() == MainAdapter.LIST_VIEW) {
                    helper.switchLayout(MainAdapter.GRID_VIEW);
                    item.setIcon(R.drawable.abc_ic_icon_list);
                    SettingsUtils.applySettings(PREFS_VIEW_MODE, MainAdapter.GRID_VIEW);
                } else {
                    helper.switchLayout(MainAdapter.LIST_VIEW);
                    item.setIcon(R.drawable.abc_ic_icon_grid);
                    SettingsUtils.applySettings(PREFS_VIEW_MODE, MainAdapter.LIST_VIEW);
                }
        }
        return false;
    }


    private String getHomeDirectory() {
        String defaultHome = StorageUtils.getSdCardPath();
        if (!rememberPath)
            return defaultHome;

        homeDirectory = SettingsUtils.getString(PREFS_HOME, defaultHome);
        return homeDirectory;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rememberPath)
            SettingsUtils.applySettings(PREFS_HOME, managerUtils.getCurrentDirectory());
    }

    @Override
    public void onBackPressed() {
        if (helper.isFabOpened()) {
            helper.hideFAB();
            return;
        }
        String curDir = managerUtils.getCurrentDirectory();
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (curDir.equals(getHomeDirectory())) {
                super.onBackPressed();
            } else {
                helper.updateDirectory(managerUtils.getPreviousDirectory());
                pathLabelView.goToPrevious();
            }
        }
    }


    public void share() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        //intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + currentSelectedFile.getPath()));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Sharing file..");
        intent.putExtra(Intent.EXTRA_TEXT, "Sharing file..");
        startActivity(Intent.createChooser(intent, "Share file.."));
    }

    @Override
    public void onItemClick(View view, int position) {
        FileItem item = mainAdapter.getItem(position);
        File file = new File(item.getPath());
        String name = item.getName();

        if (actionMode != null && mainAdapter.isMultiSelectEnabled()) {
            mainAdapter.selectItem(position);

        } else {
            if (file.isDirectory()) {
                try {
                    helper.updateDirectory(managerUtils.getNextDirectory(name, false));
                    pathLabelView.updatePathLabel(name, false, true);
                } catch (Exception e) {
                    Snackbar.make(view, "Can't open this folder", Snackbar.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                try {
                    FileUtils.openFile(this, file);
                } catch (Exception e) {
                    Snackbar.make(view, "Can't open this file", Snackbar.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onItemLongClick(View view, int position) {
        if (actionMode == null && !mainAdapter.isMultiSelectEnabled()) {
            dragSelectTouchListener.startDragSelection(position);
            mainAdapter.setMultiSelectEnabled(true);
            mainAdapter.selectItem(position);
            recyclerView.startActionModeForChild(view, this);
            return true;
        }

        return false;
    }

    @Override
    public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
        actionMode = mode;
        String selection = String.valueOf(mainAdapter.getSelectedItemsCount());
        mode.setTitle(selection);
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.cab_menu, menu);
        AnimationUtils.animateToToolBar(fab, bottomNavigationView);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(android.view.ActionMode mode) {
        mainAdapter.setMultiSelectEnabled(false);
        actionMode = null;
        AnimationUtils.animateToFAB(fab, bottomNavigationView);
    }

    @Override
    public boolean onOptionItemClick(MenuItem menuItem, int position) {
        File file = new File(mainAdapter.getItem(position).getPath());
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.fom_delete:
                if (SystemUtil.isKitkat()) {
                    if (FileUtils.isOnExtSdCard(file)) {
                        if (SettingsUtils.getTreeUri() == null)
                            MainDialogFragment.newInstance(MainDialogFragment.SAF_TYPE_DIALOG).show(getSupportFragmentManager(), "");
                        return true;
                    }
                }
                if (FileUtils.deleteFile(file)) {
                    mainAdapter.removeItem(position);
                    return true;
                } else
                    return false;

            case R.id.fom_details:
                MainDialogFragment dialogFragment = MainDialogFragment.newInstance(MainDialogFragment.FILE_DETAILS_DIALOG);
                dialogFragment.setFile(file);
                helper.showDialog(dialogFragment);
                return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MainDialogFragment.PICK_SD_PATH_CODE && resultCode == RESULT_OK) {
            Uri treeUri = data.getData();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                }
            };

            SettingsUtils.applySettings(PREFS_TREE_URI, treeUri.toString());
            getContentResolver().takePersistableUriPermission(treeUri, (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));

        }
    }

    public MainActivityHelper getHelper() {
        return helper;
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String path;
        if (id == R.id.nav_sd_card) {
            path = StorageUtils.getExtSdCardPaths(this);
            if (!managerUtils.getCurrentDirectory().equals(path)) {
                helper.updateDirectory(managerUtils.setHomeDirectory(path));
                pathLabelView.changeVolume(managerUtils.getCurrentDirectory());
                SettingsUtils.applySettings(PREFS_HOME, path);
            }
        } else if (id == R.id.nav_device) {
            path = StorageUtils.getSdCardPath();
            if (!managerUtils.getCurrentDirectory().equals(path)) {
                helper.updateDirectory(managerUtils.setHomeDirectory(path));
                pathLabelView.changeVolume(managerUtils.getCurrentDirectory());
                SettingsUtils.applySettings(PREFS_HOME, path);
            }
        } else if (id == R.id.nav_root) {
            path = StorageUtils.getRootPath();
            if (!managerUtils.getCurrentDirectory().equals(path)) {
                helper.updateDirectory(managerUtils.setHomeDirectory(path));
                pathLabelView.changeVolume(managerUtils.getCurrentDirectory());
                SettingsUtils.applySettings(PREFS_HOME, path);
            }
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void selection(int count) {
        if (actionMode != null)
            actionMode.setTitle(String.valueOf(mainAdapter.getSelectedItemsCount()));
    }

    @Override
    public void onSelectChange(int start, int end, boolean isSelected) {
        mainAdapter.selectRange(start, end, isSelected);
    }
}
