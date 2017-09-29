package com.sachin.filemanager.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.sachin.filemanager.adapters.MainAdapter;
import com.sachin.filemanager.ui.FileItem;

import java.io.File;
import java.lang.ref.WeakReference;

public class IconLoader extends AsyncTask<File, Void, Bitmap> {
    private WeakReference<FileItem> weakReference;
    //private HashMap<String, Drawable> hashMap;
    private String key;
    private MainAdapter adapter;

    public IconLoader(FileItem fileItem, MainAdapter adapter) {
        this.weakReference = new WeakReference<>(fileItem);
        this.adapter = adapter;
    }

    @Override
    protected Bitmap doInBackground(File... params) {
        /*File file = params[0];
        key = file.toString();
        Bitmap bitmap = null;
        if (file != null && file.isFile()) {
            if (FileUtils.isAPKFile(file)) {
                bitmap = Icons.getAPKIconBitmap(file);

            } else if (FileUtils.isImageFile(file))
                drawable = new BitmapDrawable(FileManagerApplication.getAppContext().getResources(),
                        Icons.getImagePreview(file));
        }*/

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        /*FileItem fileItem = weakReference.get();

        if (fileItem != null)
            fileItem.setIcon(drawable);

        adapter.notifyItemChanged(adapter.indexOfItem(fileItem));*/
    }
}
