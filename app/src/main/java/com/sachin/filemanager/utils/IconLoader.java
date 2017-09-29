package com.sachin.filemanager.utils;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import com.sachin.filemanager.ui.FileItem;
import com.sachin.filemanager.ui.Icons;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;

public class IconLoader extends Thread {
    private static HashMap<String, Bitmap> mCacheMap = null;
    private int mWidth;
    private int mHeight;
    private SoftReference<Bitmap> mThumb;
    private List<FileItem> mFileItems;
    private Handler mHandler;
    private boolean mStop = false;

    public IconLoader(int width, int height, boolean useThumbs) {
        mHeight = height;
        mWidth = width;

        if (mCacheMap == null)
            mCacheMap = new HashMap<>();
    }

    public Bitmap hasBitmapCached(String key) {
        return mCacheMap.get(key);
    }

    public void setCancelThumbnails(boolean stop) {
        mStop = stop;
    }

    public void createNewThumbnail(List<FileItem> fileItems, Handler handler) {
        this.mFileItems = fileItems;
        this.mHandler = handler;
    }

    @Override
    public void run() {
        int length = mFileItems.size();

        for (int i = 0; i < length; i++) {
            // cancel the operation
            if (mStop) {
                mStop = false;
                mFileItems = null;
                return;
            }

            FileItem fileItem = mFileItems.get(i);
            final File file = fileItem.getBaseFile();
            Bitmap bitmap;

            if (file.isFile()) {
                if (isImageFile(file)) {
                    bitmap = Icons.decodeSampleBitmapFromFile(file);
                    mThumb = new SoftReference<>(bitmap);
                    fileItem.setIcon(mThumb.get());
                    mCacheMap.put(file.getPath(), mThumb.get());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Message msg = mHandler.obtainMessage();
                            msg.obj = mThumb.get();
                            msg.sendToTarget();
                        }
                    });
                } else if (isApkFile(file)) {
                    bitmap = Icons.getAPKIconBitmap(file);
                    mThumb = new SoftReference<>(bitmap);
                    fileItem.setIcon(mThumb.get());
                    mCacheMap.put(file.getPath(), mThumb.get());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Message msg = mHandler.obtainMessage();
                            msg.obj = mThumb.get();
                            msg.sendToTarget();
                        }
                    });
                }
            }

        }
    }

    private boolean isApkFile(File file) {
        return file.toString().endsWith(".apk");
    }

    private boolean isImageFile(File file) {
        String ext = file.toString().substring(file.toString().lastIndexOf(".") + 1);

        if (ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("jpg") ||
                ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("gif") ||
                ext.equalsIgnoreCase("tiff") || ext.equalsIgnoreCase("tif"))
            return true;

        return false;
    }
}