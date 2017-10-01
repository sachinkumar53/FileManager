package com.sachin.filemanager.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sachin.filemanager.FileManager;
import com.sachin.filemanager.R;
import com.sachin.filemanager.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileListAdapter extends ArrayAdapter<String> {

    private List<String> fileArrayList;
    private ArrayList<String> selectedFiles; //mMultiSelectData
    private ArrayList<Integer> selectedFilePosition; //positions
    private Context context;
    private FileManager fileManager;
    private boolean multiSelect;
//    private ThumbnailCreator thumbnailCreator;
    //private ApkIconCreator iconCreator;
    private int thumbnailSize;
    private boolean thumbsAllowed;
    private boolean showFilePermissions;
    private int lastPosition = -1;
    public FileListAdapter(Context context, int resource, List<String> objects, FileManager manager) {
        super(context, resource, objects);
        this.context = context;
        fileManager = manager;
        multiSelect = false;
        thumbsAllowed = true;

        selectedFilePosition = new ArrayList<>();

        thumbnailSize = (int) context.getResources().getDimension(R.dimen.folder_icon_size);
        if (objects != null) {
            fileArrayList = objects;
        } else {
            fileArrayList = new ArrayList<>(fileManager.setHomeDir
                    (Environment.getExternalStorageDirectory().getPath()));
        }
    }

    public List<String> getFileArrayList() {
        return fileArrayList;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public boolean hasMultiSelectData() {
        return (selectedFiles != null && selectedFiles.size() > 0);
    }

    public void setThumbsAllowed(boolean value) {
        this.thumbsAllowed = value;
        notifyDataSetChanged();
    }

    public void setShowFilePermissions(boolean enable) {
        this.showFilePermissions = enable;
        notifyDataSetChanged();
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;

        if (!multiSelect)
            dismissMultiSelect(true);
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        if (selectedFiles != null && selectedFiles.size() > 0) {
            return selectedFiles.size();
        } else {
            return 0;
        }
    }

    public void selectItem(int position, String path) {

        if (selectedFilePosition == null)
            selectedFilePosition = new ArrayList<>();

        if (selectedFiles == null) {
            selectedFilePosition.add(position);
            addToMultiSelectFile(path);

        } else if (selectedFiles.contains(path)) {
            if (selectedFilePosition.contains(position))
                selectedFilePosition.remove(new Integer(position));

            selectedFiles.remove(path);

        } else {
            selectedFilePosition.add(position);
            addToMultiSelectFile(path);
        }

        notifyDataSetChanged();
    }

    private void addToMultiSelectFile(String src) {
        if (selectedFiles == null)
            selectedFiles = new ArrayList<>();

        selectedFiles.add(src);
    }

    private void dismissMultiSelect(boolean clearData) {
        multiSelect = false;

        if (selectedFilePosition != null && !selectedFilePosition.isEmpty())
            selectedFilePosition.clear();

        if (clearData)
            if (selectedFiles != null && !selectedFiles.isEmpty())
                selectedFiles.clear();

        notifyDataSetChanged();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return fileArrayList.get(position);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        lastPosition = -1;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder viewHolder;

        String currentDir = fileManager.getCurrentDir();
        final File file = new File(currentDir + "/" + fileArrayList.get(position));

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_layout_item_list, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.itemIcon = (ImageView) convertView.findViewById(R.id.row_image);
            viewHolder.itemName = (TextView) convertView.findViewById(R.id.top_view);
            viewHolder.itemDetails = (TextView) convertView.findViewById(R.id.bottom_view);
            viewHolder.itemOptions = (ImageView) convertView.findViewById(R.id.more_file_options);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        lastPosition = position;

        if (selectedFilePosition != null && selectedFilePosition.contains(position))
            //convertView.setBackgroundColor(context.getResources().getColor(R.color.selected));
            //else
            convertView.setBackgroundColor(Color.TRANSPARENT);


        //Drawable drawable = FileUtil.getFileIcon(context,file);
        //viewHolder.itemIcon.setImageDrawable(drawable);

        if (thumbsAllowed && file != null && file.isFile()) {
            String ext = file.toString();
            String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);

  /*          if (thumbnailCreator == null) {
                thumbnailCreator = new ThumbnailCreator(thumbnailSize, thumbnailSize);
            }
*/
            // if (iconCreator == null) {
            //   iconCreator = new ApkIconCreator(context);
            //}

            if (FileUtil.identify(sub_ext).equals(FileUtil.TYPE_IMAGE)) {

                if (file.length() != 0) {
                    //                  Bitmap thumb = thumbnailCreator.isBitmapCached(file.getPath());

    /*                if (thumb == null) {
                        final Handler handle = new Handler(new Handler.Callback() {
                            public boolean handleMessage(Message msg) {
                                notifyDataSetChanged();
                                return true;
                            }
                        });

                        //thumbnailCreator.createNewThumbnail(fileArrayList, fileManager.getCurrentDir(), handle);

                        if (!thumbnailCreator.isAlive()) {
                            if (thumbnailCreator.isInterrupted()) {
                                thumbnailCreator.start();
                            }
                        }

                    } else {
                        viewHolder.itemIcon.setImageBitmap(thumb);
                    }

                } else {
                    //viewHolder.itemIcon.setImageResource(R.drawable.myfiles_file_images);
                }
*/
                } else if (FileUtil.identify(sub_ext).equals(FileUtil.TYPE_APK)) {
                    // Drawable appIcon = iconCreator.isApkIconCached(file.getPath());
                    if (file.length() != 0) {
                    /*if (appIcon == null) {
                        final Handler handler = new Handler(new Handler.Callback() {
                            public boolean handleMessage(Message msg) {
                                notifyDataSetChanged();
                                return true;
                            }
                        });

                        //iconCreator.createIcon(fileArrayList, fileManager.getCurrentDir(), handler);

                        if (!iconCreator.isAlive()) {
                            if (iconCreator.isInterrupted()) {
                                iconCreator.start();
                            }
                        }
                    } else {
                        viewHolder.itemIcon.setImageDrawable(appIcon);
                    }*/
                    } else {
                        // viewHolder.itemIcon.setImageResource(R.drawable.ic_file_apk_mtrl);
                    }

                    //viewHolder.itemIcon.setImageDrawable(iconCreator.getApkIcon(file));

                }
            }
        }

        String fileSize = FileUtil.calculateSize(file);

        if (showFilePermissions)
            viewHolder.itemDetails.setText(fileSize + FileUtil.getFilePermissions(file));
        else
            viewHolder.itemDetails.setText(fileSize);

        viewHolder.itemName.setText(file.getName());

        return convertView;
    }

    private class ViewHolder {
        private ImageView itemIcon;
        private TextView itemName;
        private TextView itemDetails;
        private ImageView itemOptions;
    }
}
