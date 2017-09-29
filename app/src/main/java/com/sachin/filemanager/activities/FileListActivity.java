package com.sachin.filemanager.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sachin.filemanager.EventHandler;
import com.sachin.filemanager.FileManager;
import com.sachin.filemanager.R;
import com.sachin.filemanager.constants.KEYS;
import com.sachin.filemanager.fragments.SettingsFragment;

import java.io.File;


public class FileListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener,
        KEYS{

    public static final String ACTION_WIDGET = "com.nexes.manager.Main.ACTION_WIDGET";

    public static final String ACTION_SETTINGS_CHANGED = "com.sachin.filemanager.SETTINGS_CHANGED";

    private static final int MENU_SEARCH =  0x02;
    private static final int SEARCH_B = 	0x09;

    private FileManager fileManager;
    private EventHandler eventHandler;
    private EventHandler.FileItemAdapter fileItemAdapter;
    private String homeDir;

    private SharedPreferences mSettings;
    private boolean mReturnIntent = false;
    private boolean mHoldingZip = false;
    private boolean mUseBackKey = true;
    private String mSelectedListItem;
    private TextView mPathLabel;
    private TextView animPathLabel;

    private Animation slideIn;
    private Animation slideOut;

    private String path,pathLabel;

    private StringBuilder pathBuilder;

    private StringBuilder oldDir;
    private StringBuilder newDir;

    private BroadcastReceiver settingsReceiver;
    private IntentFilter filter;

    private ListView mFileListView;

    public static boolean mHoldingFile = false;
    public static String mCopiedTarget;
    public static String mZippedTarget;
    public static int customTitleButtonActionType = 0;   /*0 is copy
                                                          *1 is move
                                                          *2 is paste
                                                          *3 is delete */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            toolbar.setElevation(0f);
        }

        fileManager = new  FileManager();
        mSettings = PreferenceManager.getDefaultSharedPreferences(FileListActivity.this);

        if (savedInstanceState != null)
            eventHandler = new EventHandler(FileListActivity.this, fileManager, savedInstanceState.getString("location"),false);
        else
            eventHandler = new EventHandler(FileListActivity.this, fileManager,getHomeDir(),true);

        init();
        updateUI();
    }

    private void init() {

        filter = new IntentFilter(ACTION_SETTINGS_CHANGED);

        settingsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUI();
            }
        };
        registerReceiver(settingsReceiver, filter);

        fileItemAdapter = eventHandler.new FileItemAdapter();

        eventHandler.setListAdapter(fileItemAdapter);

        mFileListView = (ListView)findViewById(R.id.file_list);
        mFileListView.setAdapter(fileItemAdapter);
        mFileListView.setOnItemClickListener(this);
        mFileListView.setOnItemLongClickListener(this);

        pathBuilder = new StringBuilder();
        oldDir = pathBuilder.append(fileManager.getCurrentDir());
        newDir = new StringBuilder();

        Intent intent = getIntent();

        if(intent.getAction().equals(Intent.ACTION_GET_CONTENT)) {
            mReturnIntent = true;

        } else if (intent.getAction().equals(ACTION_WIDGET)) {
            Log.e("MAIN", "Widget action, string = " + intent.getExtras().getString("folder"));
            eventHandler.updateDirectory(fileManager.getNextDir(intent.getExtras().getString("folder"), true));

        }
    }

    private void updatePathLabel(String oldDir, final String newDir) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("location", fileManager.getCurrentDir());
    }

    private String getHomeDir(){
        String defaultHome = Environment.getExternalStorageDirectory().getPath();
        homeDir = mSettings.getString(PREFS_HOME,defaultHome);
        return homeDir;
    }


     /* Returns the file that was selected to the intent that
     * called this activity. usually from the caller is another application.
     */
    private void returnIntentResults(File data) {
        mReturnIntent = false;
        Intent ret = new Intent();
        ret.setData(Uri.fromFile(data));
        setResult(RESULT_OK, ret);
        finish();
    }


    private void updateUI(){
        boolean hidden = mSettings.getBoolean(PREFS_HIDDEN, false);
        boolean thumbnail = mSettings.getBoolean(PREFS_THUMBS, true);
        int sort = mSettings.getInt(PREFS_SORT, 1);

        fileManager.setHomeDir(getHomeDir());
        fileManager.setSortType(sort);
        fileManager.setShowHiddenFiles(hidden);
        eventHandler.setShowThumbnails(thumbnail);
        eventHandler.updateDirectory(fileManager.getNextDir(fileManager.getCurrentDir(), true));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final String item = eventHandler.getData(position);
        boolean multiSelect = eventHandler.isMultiSelected();
        String curr = fileManager.getCurrentDir();

        if (!multiSelect){
            mSelectedListItem = item;
        }else {
            invalidateOptionsMenu();
        }
        File file = new File(fileManager.getCurrentDir() + "/" + item);
        String item_ext = null;

        try {
            item_ext = item.substring(item.lastIndexOf("."), item.length());

        } catch(IndexOutOfBoundsException e) {
            item_ext = "";
        }
        if(multiSelect) {
            addToMultiSelect(position, file.getPath());
        } else {
            if (file.isDirectory()) {
                if(file.canRead()) {
                    eventHandler.stopThumbnailThread();
                    eventHandler.updateDirectory(fileManager.getNextDir(item, false));
                    updatePathLabel(curr,curr+"/"+item);
		    		/*set back button switch to true
		    		 * (this will be better implemented later)
		    		 */
                    if(!mUseBackKey)
                        mUseBackKey = true;

                } else {
                    Toast.makeText(this, "Can't read folder due to permissions",
                            Toast.LENGTH_SHORT).show();
                }
            }

	    	/*music file selected--add more audio formats*/
            else if (item_ext.equalsIgnoreCase(".mp3") ||
                    item_ext.equalsIgnoreCase(".m4a")||
                    item_ext.equalsIgnoreCase(".mp4")) {

                if(mReturnIntent) {
                    returnIntentResults(file);
                } else {
                    Intent i = new Intent();
                    i.setAction(android.content.Intent.ACTION_VIEW);
                    i.setDataAndType(Uri.fromFile(file), "audio/*");
                    startActivity(i);
                }
            }

	    	/*photo file selected*/
            else if(item_ext.equalsIgnoreCase(".jpeg") ||
                    item_ext.equalsIgnoreCase(".jpg")  ||
                    item_ext.equalsIgnoreCase(".png")  ||
                    item_ext.equalsIgnoreCase(".gif")  ||
                    item_ext.equalsIgnoreCase(".tiff")) {

                if (file.exists()) {
                    if(mReturnIntent) {
                        returnIntentResults(file);

                    } else {
                        Intent picIntent = new Intent();
                        picIntent.setAction(android.content.Intent.ACTION_VIEW);
                        picIntent.setDataAndType(Uri.fromFile(file), "image/*");
                        startActivity(picIntent);
                    }
                }
            }

	    	/*video file selected--add more video formats*/
            else if(item_ext.equalsIgnoreCase(".m4v") ||
                    item_ext.equalsIgnoreCase(".3gp") ||
                    item_ext.equalsIgnoreCase(".wmv") ||
                    item_ext.equalsIgnoreCase(".mp4") ||
                    item_ext.equalsIgnoreCase(".ogg") ||
                    item_ext.equalsIgnoreCase(".wav")) {

                if (file.exists()) {
                    if(mReturnIntent) {
                        returnIntentResults(file);

                    } else {
                        Intent movieIntent = new Intent();
                        movieIntent.setAction(android.content.Intent.ACTION_VIEW);
                        movieIntent.setDataAndType(Uri.fromFile(file), "video/*");
                        startActivity(movieIntent);
                    }
                }
            }

	    	/*zip file */
            else if(item_ext.equalsIgnoreCase(".zip")) {

                if(mReturnIntent) {
                    returnIntentResults(file);

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    AlertDialog alert;
                    mZippedTarget = fileManager.getCurrentDir() + "/" + item;
                    CharSequence[] option = {"Extract here", "Extract to..."};

                    builder.setTitle("Extract");
                    builder.setItems(option, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            switch(which) {
                                case 0:
                                    String dir = fileManager.getCurrentDir();
                                    eventHandler.unZipFile(item, dir + "/");
                                    break;

                                case 1:
                                    mHoldingZip = true;
                                    break;
                            }
                        }
                    });

                    alert = builder.create();
                    alert.show();
                }
            }

	    	/* gzip files, this will be implemented later */
            else if(item_ext.equalsIgnoreCase(".gzip") ||
                    item_ext.equalsIgnoreCase(".gz")) {

                if(mReturnIntent) {
                    returnIntentResults(file);

                } else {

                }
            }

	    	/*pdf file selected*/
            else if(item_ext.equalsIgnoreCase(".pdf")) {

                if(file.exists()) {
                    if(mReturnIntent) {
                        returnIntentResults(file);

                    } else {
                        Intent pdfIntent = new Intent();
                        pdfIntent.setAction(android.content.Intent.ACTION_VIEW);
                        pdfIntent.setDataAndType(Uri.fromFile(file),
                                "application/pdf");

                        try {
                            startActivity(pdfIntent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(this, "Sorry, couldn't find a pdf viewer",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

	    	/*Android application file*/
            else if(item_ext.equalsIgnoreCase(".apk")){

                if(file.exists()) {
                    if(mReturnIntent) {
                        returnIntentResults(file);

                    } else {
                        Intent apkIntent = new Intent();
                        apkIntent.setAction(android.content.Intent.ACTION_VIEW);
                        apkIntent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                        startActivity(apkIntent);
                    }
                }
            }

	    	/* HTML file */
            else if(item_ext.equalsIgnoreCase(".html")) {

                if(file.exists()) {
                    if(mReturnIntent) {
                        returnIntentResults(file);

                    } else {
                        Intent htmlIntent = new Intent();
                        htmlIntent.setAction(android.content.Intent.ACTION_VIEW);
                        htmlIntent.setDataAndType(Uri.fromFile(file), "text/html");

                        try {
                            startActivity(htmlIntent);
                        } catch(ActivityNotFoundException e) {
                            Toast.makeText(this, "Sorry, couldn't find a HTML viewer",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

	    	/* text file*/
            else if(item_ext.equalsIgnoreCase(".txt")) {

                if(file.exists()) {
                    if(mReturnIntent) {
                        returnIntentResults(file);

                    } else {
                        Intent txtIntent = new Intent();
                        txtIntent.setAction(android.content.Intent.ACTION_VIEW);
                        txtIntent.setDataAndType(Uri.fromFile(file), "text/plain");

                        try {
                            startActivity(txtIntent);
                        } catch(ActivityNotFoundException e) {
                            txtIntent.setType("text/*");
                            startActivity(txtIntent);
                        }
                    }
                }
            }

	    	/* generic intent */
            else {
                if(file.exists()) {
                    if(mReturnIntent) {
                        returnIntentResults(file);

                    } else {
                        Intent generic = new Intent();
                        generic.setAction(android.content.Intent.ACTION_VIEW);
                        generic.setDataAndType(Uri.fromFile(file), "text/plain");

                        try {
                            startActivity(generic);
                        } catch(ActivityNotFoundException e) {
                            Toast.makeText(this, "Sorry, couldn't find anything " +
                                            "to open " + file.getName(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        File file = new File(fileManager.getCurrentDir() + "/" + eventHandler.getData(position));
        if (!isMultiSelect()){
            mSelectedListItem = eventHandler.getData(position);
            setMultiSelect(true);
            addToMultiSelect(position,file.getPath());
            invalidateOptionsMenu();
            return true;
        }
        return false;
    }

    private boolean isMultiSelect(){
        return eventHandler.isMultiSelected();
    }

    private void setMultiSelect(boolean value){
        eventHandler.setMultiSelect(value);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(settingsReceiver);
    }

    /* ================Menus, options menu and context menu start here=================*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.myfiles_options_menu, menu);
        MenuItem item = menu.findItem(R.id.menu_id_search);

        item.setIcon(android.support.v7.appcompat.R.drawable.abc_ic_search_api_material);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case R.id.menu_id_copy:
                eventHandler.setMultiSelect(true);
                customTitleButtonActionType = 0;
                return true;
            case R.id.menu_id_move:
                eventHandler.setMultiSelect(true);
                customTitleButtonActionType = 1;
                return true;
            case R.id.menu_id_delete:
                eventHandler.setMultiSelect(true);
                customTitleButtonActionType = 3;
                return true;

            case R.id.menu_id_sort:

                return true;

            case R.id.menu_id_settings:
                Intent intent = new Intent(this, SettingsFragment.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, v, info);
        if (!eventHandler.hasMultiSelectData()){
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.myfiles_context_menu,menu);
            AdapterView.AdapterContextMenuInfo _info = (AdapterView.AdapterContextMenuInfo)info;
            mSelectedListItem = eventHandler.getData(_info.position);

            menu.setHeaderTitle(mSelectedListItem);
            menu.removeItem(R.id.menu_zip);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_share:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + fileManager.getCurrentDir() + "/" + mSelectedListItem));
                intent.putExtra(Intent.EXTRA_SUBJECT,"Sharing file..");
                intent.putExtra(Intent.EXTRA_TEXT,"Sharing file..");
                startActivity(Intent.createChooser(intent, "Share file.."));
            case R.id.menu_delete:
                final AlertDialog.Builder builder = new AlertDialog.Builder(FileListActivity.this);
                final AlertDialog dialog = builder.create();
                builder.setTitle(getString(R.string.myfiles_delete_dialog_title));
                builder.setMessage("Deleting " + mSelectedListItem +
                        " cannot be undone. Are you sure you want to delete?");

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.cancel();
                    }
                });
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        eventHandler.deleteFile(fileManager.getCurrentDir() + "/" + mSelectedListItem);
                    }
                });

                dialog.show();
                return true;

            case R.id.menu_details:


                break;

            case R.id.menu_move:
                eventHandler.setDeleteAfterCopy(true);
                mHoldingFile = true;
                mCopiedTarget = fileManager.getCurrentDir() +"/"+ mSelectedListItem;

                return true;

            case R.id.menu_copy:
                eventHandler.setDeleteAfterCopy(false);
                mHoldingFile = true;
                mCopiedTarget = fileManager.getCurrentDir() +"/"+ mSelectedListItem;
                return true;

            case R.id.menu_zip:
                String dir = fileManager.getCurrentDir();

                eventHandler.zipFile(dir + "/" + mSelectedListItem);
                return true;
        }
        return false;
    }

    private void addToMultiSelect(int position, String path){
        fileItemAdapter.addMultiPosition(position,path);
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        final Dialog dialog = new Dialog(FileListActivity.this);
        switch(id) {
            case MENU_SEARCH:
                dialog.setContentView(R.layout.input_layout);
                dialog.setTitle("Search");
                dialog.setCancelable(false);

                final EditText search_input = (EditText)dialog.findViewById(R.id.input_field);

                Button search_button =  new Button(getBaseContext());
                Button cancel_button = new Button(getBaseContext());
                search_button.setText("Search");

                search_button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String temp = search_input.getText().toString();

                        if (temp.length() > 0)
                            eventHandler.searchForFile(temp);
                        dialog.dismiss();
                    }
                });

                cancel_button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) { dialog.dismiss(); }
                });

                break;
        }
        return dialog;
    }

    public void dismissMultiSelect(boolean updatePath){
        if (eventHandler.isMultiSelected())
        fileItemAdapter.killMultiSelect(true);
        invalidateOptionsMenu();
        if (updatePath) {
            eventHandler.refreshDirectory();
        }

    }

    @Override
    public void onBackPressed() {
        String current = fileManager.getCurrentDir();
        if(eventHandler.isMultiSelected()) {
            dismissMultiSelect(false);
            Toast.makeText(FileListActivity.this, "Multi-select is now off", Toast.LENGTH_SHORT).show();

        } else if (current == getHomeDir()){
            finish();
        }else {
            eventHandler.stopThumbnailThread();
            eventHandler.updateDirectory(fileManager.getPreviousDir());

        }
    }
}

