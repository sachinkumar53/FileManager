package com.sachin.filemanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sachin.filemanager.activities.FileListActivity;
import com.sachin.filemanager.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

public class EventHandler implements View.OnClickListener {

    private static final int SEARCH_TYPE =		0x00;
	private static final int COPY_TYPE =		0x01;
	private static final int UNZIP_TYPE =		0x02;
	private static final int UNZIPTO_TYPE =		0x03;
	private static final int ZIP_TYPE =			0x04;
	private static final int DELETE_TYPE = 		0x05;
	
	private final Context mContext;
	private final FileManager mFileMang;
	//private IconHelper mThumbnail;
	//private ApkIconCreator apkIcon;
	private FileItemAdapter mDelegate;
	
	private boolean multi_select_flag = false;
	private boolean delete_after_copy = false;
	private boolean thumbnail_flag = true;
	//the list used to feed info into the array adapter and when multi-select is on
	private ArrayList<String> mDataSource, mMultiSelectData;
	private TextView mInfoLabel;

	public EventHandler(Context context, final FileManager manager, String location,boolean setHome) {
		mContext = context;
		mFileMang = manager;
		if (setHome){
			mDataSource = new ArrayList<String>(mFileMang.setHomeDir(location));
		}else {
			mDataSource = new ArrayList<String>(mFileMang.getNextDir(location, true));
		}
	}

	public void setDefaultHome(String defaultHome){
		if (defaultHome != null){
			if (!mDataSource.isEmpty()){
				mDataSource.clear();
			}
			mDataSource = new ArrayList<String>(mFileMang.setHomeDir(defaultHome));
			mDelegate.notifyDataSetChanged();
		}
	}


	public void setListAdapter(FileItemAdapter adapter) {
		mDelegate = adapter;
	}
	

	public void setShowThumbnails(boolean show) {
		thumbnail_flag = show;
	}
	
	/**
	 * If you want to move a file (cut/paste) and not just copy/paste use this method to 
	 * tell the file manager to delete the old reference of the file.
	 * 
	 * @param delete true if you want to move a file, false to copy the file
	 */
	public void setDeleteAfterCopy(boolean delete) {
		delete_after_copy = delete;
	}
	
	/**
	 * Indicates whether the user wants to select 
	 * multiple files or folders at a time.
	 * <br><br>
	 * false by default
	 * 
	 * @return	true if the user has turned on multi selection
	 */
	public boolean isMultiSelected() {
		return multi_select_flag;
	}
	
	/**
	 * Use this method to determine if the user has selected multiple files/folders
	 * 
	 * @return	returns true if the user is holding multiple objects (multi-select)
	 */
	public boolean hasMultiSelectData() {
		return (mMultiSelectData != null && mMultiSelectData.size() > 0);
	}

	public boolean isSingleItemSelected(){
        boolean b = false;
        if (mMultiSelectData != null && hasMultiSelectData()) {
            if (mMultiSelectData.size() == 1)
                b = true;
        }
        return b;
	}
	
	/**
	 * Will search for a file then display all files with the 
	 * search parameter in its name
	 * 
	 * @param name	the name to search for
	 */
	public void searchForFile(String name) {
		new BackgroundWork(SEARCH_TYPE).execute(name);
	}
	

	public void deleteFile(String name) {
        new BackgroundWork(DELETE_TYPE).execute(name);
	}
	
	/**
	 * Will copy a file or folder to another location.
	 * 
	 * @param oldLocation	from location
	 * @param newLocation	to location
	 */
	public void copyFile(String oldLocation, String newLocation) {
		String[] data = {oldLocation, newLocation};
		
		new BackgroundWork(COPY_TYPE).execute(data);
	}
	
	/**
	 * 
	 * @param newLocation
	 */
	public void copyFileMultiSelect(String newLocation) {
		String[] data;
		int index = 1;
		
		if (mMultiSelectData.size() > 0) {
			data = new String[mMultiSelectData.size() + 1];
			data[0] = newLocation;
			
			for(String s : mMultiSelectData)
				data[index++] = s;
			
			new BackgroundWork(COPY_TYPE).execute(data);
		}
	}

	public void unZipFile(String file, String path) {
		new BackgroundWork(UNZIP_TYPE).execute(file, path);
	}
	
	/**
	 * This method will take a zip file and extract it to another
	 * location
	 *  
	 * @param name		the name of the of the new file (the dir name is used)
	 * @param newDir	the dir where to extract to
	 * @param oldDir	the dir where the zip file is
	 */
	public void unZipFileToDir(String name, String newDir, String oldDir) {
		new BackgroundWork(UNZIPTO_TYPE).execute(name, newDir, oldDir);
	}
	
	/**
	 * Creates a zip file
	 * 
	 * @param zipPath	the path to the directory you want to zip
	 */
	public void zipFile(String zipPath) {
		new BackgroundWork(ZIP_TYPE).execute(zipPath);
	}
	
	/**
	 * this will stop our background thread that creates thumbnail icons
	 * if the thread is running. this should be stopped when ever 
	 * we leave the folder the image files are in.
	 */
	public void stopThumbnailThread() {
/*
		if (mThumbnail != null) {
			mThumbnail.setCancelThumbnails(true);
			mThumbnail = null;
		}
*/
	}

	/*public void stopApkIconThread() {
		if (apkIcon != null) {
			apkIcon.cancelApkIcon(true);
			apkIcon = null;
		}
	}*/
	

	public String getData(int position) {
		
		if(position > mDataSource.size() - 1 || position < 0)
			return null;
		
		return mDataSource.get(position);
	}

	public void setMultiSelect(boolean multi_select){
		multi_select_flag = multi_select;
	}

	/**
	 * called to update the file contents as the user navigates there
	 * phones file system. 
	 * 
	 * @param content	an ArrayList of the file/folders in the current directory.
	 */
	public void updateDirectory(ArrayList<String> content) {	
		if(!mDataSource.isEmpty())
			mDataSource.clear();
		
		for(String data : content)
			mDataSource.add(data);
		
		mDelegate.notifyDataSetChanged();
	}

	public void goToDirectory(String path) {
		ArrayList<String> list = mFileMang.getNextDir(path,true);
		if(!mDataSource.isEmpty())
			mDataSource.clear();

		for(String data : list)
			mDataSource.add(data);

		mDelegate.notifyDataSetChanged();
	}

	public void refreshDirectory(){
		String temp = mFileMang.getCurrentDir();
		updateDirectory(mFileMang.getNextDir(temp, true));
	}

	@Override
	public void onClick(View view) {
		int i = 0;
		switch(view.getId()) {
			case R.id.media_close_button:
				switch (i) {
					case 0:
						if (hasMultiSelectData()){
							setMultiSelect(false);
							delete_after_copy = false;

							mInfoLabel.setText("Holding for copy " + mMultiSelectData.size() +
									" file(s)");
						}else {
							mDelegate.killMultiSelect(true);
						}
						break;
					case 1:
						if (hasMultiSelectData()){
							delete_after_copy = true;
							setMultiSelect(false);

							mInfoLabel.setText("Holding for move " + mMultiSelectData.size() +
									" file(s)");
						}else {
							mDelegate.killMultiSelect(true);
						}

						break;
					case 2:
						if(hasMultiSelectData()){
							copyFileMultiSelect(mFileMang.getCurrentDir());

						}else if(FileListActivity.mHoldingFile && FileListActivity.mCopiedTarget.length() > 1){
							copyFile(FileListActivity.mCopiedTarget, mFileMang.getCurrentDir());
							mInfoLabel.setText("");
						}
						FileListActivity.mHoldingFile = false;
						break;
					case 3:
						setMultiSelect(false);
						final String[] data = new String[mMultiSelectData.size()];
						int at = 0;

						for (String string : mMultiSelectData)
							data[at++] = string;

						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						builder.setMessage("Are you sure you want to delete " +
								data.length + " files? This cannot be " +
								"undone.");
						builder.setCancelable(false);
						builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								new BackgroundWork(DELETE_TYPE).execute(data);
								mDelegate.killMultiSelect(true);
							}
						});
						builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								mDelegate.killMultiSelect(true);
								dialog.cancel();
							}
						});

						builder.create().show();
						break;
					default:
						break;
				}
		}
	}

	private static class ViewHolder {
		TextView topView;
		TextView bottomView;
		ImageView icon;
		CheckBox mSelect;	//multi-select check mark icon
	}


    public class FileItemAdapter extends ArrayAdapter<String> {
    	private final int KB = 1024;
    	private final int MG = KB * KB;
    	private final int GB = MG * KB;    	
    	private String display_size;
    	private ArrayList<Integer> positions;

    	public FileItemAdapter() {
    		super(mContext, R.layout.row_layout_item_list, mDataSource);
    	}
    	
    	public void addMultiPosition(int index, String path) {
    		if(positions == null)
    			positions = new ArrayList<Integer>();
    		
    		if(mMultiSelectData == null) {
    			positions.add(index);
    			add_multiSelect_file(path);
    			
    		} else if(mMultiSelectData.contains(path)) {
    			if(positions.contains(index))
    				positions.remove(new Integer(index));
    			
    			mMultiSelectData.remove(path);
    			
    		} else {
    			positions.add(index);
    			add_multiSelect_file(path);
    		}
    		
    		notifyDataSetChanged();
    	}

		public void killMultiSelect(boolean clearData) {
    		multi_select_flag = false;
    		
    		if(positions != null && !positions.isEmpty())
    			positions.clear();
    		
    		if(clearData)
    			if(mMultiSelectData != null && !mMultiSelectData.isEmpty())
    				mMultiSelectData.clear();
    		
    		notifyDataSetChanged();
    	}
    	
    	public String getFilePermissions(File file) {
    		String per = "-";
    	    		
    		if(file.isDirectory())
    			per += "d";
    		if(file.canRead())
    			per += "r";
    		if(file.canWrite())
    			per += "w";
    		
    		return per;
    	}
    	
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
        	final ViewHolder mViewHolder;
    		int num_items = 0;
    		String temp = mFileMang.getCurrentDir();
    		File file = new File(temp + "/" + mDataSource.get(position));
    		String[] list = file.list();
    		
    		if(list != null)
    			num_items = list.length;
   
    		if(convertView == null) {
    			LayoutInflater inflater = (LayoutInflater) mContext.
    						getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			convertView = inflater.inflate(R.layout.row_layout_item_list, parent, false);

    			mViewHolder = new ViewHolder();
    			mViewHolder.topView = (TextView)convertView.findViewById(R.id.top_view);
    			mViewHolder.bottomView = (TextView)convertView.findViewById(R.id.bottom_view);
    			mViewHolder.icon = (ImageView)convertView.findViewById(R.id.row_image);
    			convertView.setTag(mViewHolder);
    			
    		} else {
    			mViewHolder = (ViewHolder)convertView.getTag();
    		}   			
			if (isMultiSelected()){
				mViewHolder.mSelect.setVisibility(View.VISIBLE);
			}else {
				mViewHolder.mSelect.setVisibility(View.GONE);
			}
    		if (positions != null && positions.contains(position))
    			mViewHolder.mSelect.setChecked(true);
    		else
    			mViewHolder.mSelect.setChecked(false);

    		/*
			if(mThumbnail == null)
    			mThumbnail = new IconHelper(52, 52,false);
*/
		/*	if (apkIcon == null){
				apkIcon = new ApkIconCreator(mContext);
			}
*/
			//Drawable drawable = FileUtils.getFileIcon(mContext,file);
			//mViewHolder.icon.setImageDrawable(drawable);

			if (thumbnail_flag && file != null && file.isFile()){
				String ext = file.toString();
				String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);

				if (FileUtils.identify(sub_ext).equals(FileUtils.TYPE_IMAGE)) {

					if (thumbnail_flag && file.length() != 0) {
						Bitmap thumb = null;//mThumbnail.isBitmapCached(file.getPath());

						if (thumb == null) {
							final Handler handle = new Handler(new Handler.Callback() {
								public boolean handleMessage(Message msg) {
									notifyDataSetChanged();
									return true;
								}
							});

							//mThumbnail.createNewThumbnail(mDataSource, mFileMang.getCurrentDir(), handle);

			/*				if (!mThumbnail.isAlive())
								mThumbnail.start();
*/
						} else {
							mViewHolder.icon.setImageBitmap(thumb);
						}

					} else {
						//mViewHolder.icon.setImageResource(R.drawable.myfiles_file_images);
					}

				} else if (FileUtils.identify(sub_ext).equals(FileUtils.TYPE_APK)) {
					//Drawable appIcon = apkIcon.isApkIconCached(file.getPath());
					if (file.length() != 0) {
						/*if (appIcon == null) {
							final Handler handler = new Handler(new Handler.Callback() {
								public boolean handleMessage(Message msg) {
									notifyDataSetChanged();
									return true;
								}
							});*/

/*
							apkIcon.createIcon(mDataSource, mFileMang.getCurrentDir(), handler);

							if (!apkIcon.isAlive()) {
								if (apkIcon.isInterrupted()) {
									apkIcon.start();
								}
							}
*/
/*
						} else {
							mViewHolder.icon.setImageDrawable(appIcon);
						}
*/
					} else {
						mViewHolder.icon.setImageResource(android.R.drawable.sym_def_app_icon);
					}

					//mViewHolder.icon.setImageDrawable(apkIcon.getApkIcon(file));

				}
			}

    		    		
    		String permission = getFilePermissions(file);
    		
    		if(file.isFile()) {
    			double size = file.length();
        		if (size > GB)
    				display_size = String.format("%.2f Gb ", (double)size / GB);
    			else if (size < GB && size > MG)
    				display_size = String.format("%.2f Mb ", (double)size / MG);
    			else if (size < MG && size > KB)
    				display_size = String.format("%.2f Kb ", (double)size/ KB);
    			else
    				display_size = String.format("%.2f bytes ", (double)size);
        		
        		if(file.isHidden())
        			mViewHolder.bottomView.setText("(hidden) | " + display_size +" | "+ permission);
        		else
        			mViewHolder.bottomView.setText(display_size +" | "+ permission);
        		
    		} else {
    			if(file.isHidden())
    				mViewHolder.bottomView.setText("(hidden) | " + num_items + " items | " + permission);
    			else
    				mViewHolder.bottomView.setText(num_items + " items | " + permission);
    		}
    		
    		mViewHolder.topView.setText(file.getName());
    		
    		return convertView;
    	}
    	
    	private void add_multiSelect_file(String src) {
    		if(mMultiSelectData == null)
    			mMultiSelectData = new ArrayList<String>();
    		
    		mMultiSelectData.add(src);
    	}
    }

    private class BackgroundWork extends AsyncTask<String, Void, ArrayList<String>> {
    	private String file_name;
    	private ProgressDialog progressDialog;
    	private int type;
    	private int copy_rtn;
    	
    	private BackgroundWork(int type) {
    		this.type = type;
    	}

    	@Override
    	protected void onPreExecute() {
			progressDialog = new ProgressDialog(mContext);
    		switch(type) {
    			case SEARCH_TYPE:
					progressDialog.setTitle("Searching");
					progressDialog.setMessage("Searching current file system...");
					progressDialog.show();
    				break;
    				
    			case COPY_TYPE:
					progressDialog.setTitle("Copying");
					progressDialog.setMessage("Copying file...");
					progressDialog.show();
    				break;
    				
    			case UNZIP_TYPE:
    				progressDialog.setTitle("Unzipping");
					progressDialog.setMessage("Unpacking zip file please wait...");
					progressDialog.show();
    				break;
    				
    			case UNZIPTO_TYPE:
    				progressDialog.setTitle("Unzipping");
					progressDialog.setMessage("Unpacking zip file please wait...");
					progressDialog.show();
    				break;
    			
    			case ZIP_TYPE:
					progressDialog.setTitle("Zipping");
					progressDialog.setMessage("Zipping folder...");
					progressDialog.show();
    				break;
    				
    			case DELETE_TYPE:
					progressDialog.setTitle("Deleting");
					progressDialog.setMessage("Deleting files...");
					progressDialog.show();
    				break;
    		}
    	}

    	/**
    	 * background thread here
    	 */
    	@Override
		protected ArrayList<String> doInBackground(String... params) {
			
			switch(type) {
				case SEARCH_TYPE:
					file_name = params[0];
					ArrayList<String> found = mFileMang.searchInDirectory(mFileMang.getCurrentDir(), 
																	    file_name);
					return found;
					
				case COPY_TYPE:
					int len = params.length;
					
					if(mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
						for(int i = 1; i < len; i++) {
							copy_rtn = mFileMang.copyToDirectory(params[i], params[0]);
							
							if(delete_after_copy)
								mFileMang.deleteTarget(params[i]);
						}
					} else {
						copy_rtn = mFileMang.copyToDirectory(params[0], params[1]);
						
						if(delete_after_copy)
							mFileMang.deleteTarget(params[0]);
					}
					
					delete_after_copy = false;
					return null;
					
				case UNZIP_TYPE:
					mFileMang.extractZipFiles(params[0], params[1]);
					return null;
					
				case UNZIPTO_TYPE:
					mFileMang.extractZipFilesFromDir(params[0], params[1], params[2]);
					return null;
					
				case ZIP_TYPE:
					mFileMang.createZipFile(params[0]);
					return null;
					
				case DELETE_TYPE:
					int size = params.length;
					
					for(int i = 0; i < size; i++)
						mFileMang.deleteTarget(params[i]);
					
					return null;
			}
			return null;
		}

    	@Override
		protected void onPostExecute(final ArrayList<String> file) {			
			final CharSequence[] names;
			int len = file != null ? file.size() : 0;
			
			switch(type) {
				case SEARCH_TYPE:				
					if(len == 0) {
						Toast.makeText(mContext, "Couldn't find " + file_name, 
											Toast.LENGTH_SHORT).show();
					
					} else {
						names = new CharSequence[len];
						
						for (int i = 0; i < len; i++) {
							String entry = file.get(i);
							names[i] = entry.substring(entry.lastIndexOf("/") + 1, entry.length());
						}
						
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						builder.setTitle("Found " + len + " file(s)");
						builder.setItems(names, new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int position) {
								String path = file.get(position);
								updateDirectory(mFileMang.getNextDir(path.
													substring(0, path.lastIndexOf("/")), true));
							}
						});
						
						AlertDialog dialog = builder.create();
						dialog.show();
					}
					
					progressDialog.dismiss();
					break;
					
				case COPY_TYPE:
					if(mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
						multi_select_flag = false;
						mMultiSelectData.clear();
					}
					
					if(copy_rtn == 0)
						Toast.makeText(mContext, "File successfully copied and pasted", 
											Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(mContext, "Copy pasted failed", Toast.LENGTH_SHORT).show();
					
					progressDialog.dismiss();
					mInfoLabel.setText("");
					break;
					
				case UNZIP_TYPE:
					updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
					progressDialog.dismiss();
					break;
					
				case UNZIPTO_TYPE:
					updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
					progressDialog.dismiss();
					break;
					
				case ZIP_TYPE:
					updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
					progressDialog.dismiss();
					break;
					
				case DELETE_TYPE:
					if(mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
						mMultiSelectData.clear();
						multi_select_flag = false;
					}
					
					updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(), true));
					progressDialog.dismiss();
					mInfoLabel.setText("");
					break;
			}
		}
    }
}
