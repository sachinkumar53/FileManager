package com.sachin.filemanager.utils;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import com.sachin.filemanager.ui.FileItem;
import com.sachin.filemanager.ui.Icons;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IconLoader extends Thread {
    private static final int MAX_CACHE = 500;

    private static HashMap<String, Bitmap> mCacheMap = null;
    private SoftReference<Bitmap> mIcon;
    private List<FileItem> mFileItems;
    private Handler mHandler;
    private boolean mStop = false;

    // Make icon size large for gridview
    //private boolean useThumbs;

    public IconLoader() {
        if (mCacheMap == null)
            mCacheMap = new LinkedHashMap<String, Bitmap>(MAX_CACHE, .75F, true) {
                private static final long serialVersionUID = 1L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Bitmap> eldest) {
                    return size() > MAX_CACHE;
                }
            };
    }

    public void clearCache() {
        if (!mCacheMap.isEmpty())
            mCacheMap.clear();

        mCacheMap = null;
    }

    public Bitmap hasLoadedCache(String key) {
        return mCacheMap.get(key);
    }

    public void cancelLoading(boolean stop) {
        mStop = stop;
    }

    public void loadIcon(List<FileItem> fileItems, Handler handler) {
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

                //Check if already has been cached
                if (hasLoadedCache(file.getPath()) != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Message msg = mHandler.obtainMessage();
                            msg.sendToTarget();
                        }
                    });
                    return;
                }

                if (isImageFile(file)) {
                    bitmap = Icons.decodeSampleBitmapFromFile(file);
                    mIcon = new SoftReference<>(bitmap);
                    fileItem.setIcon(mIcon.get());
                    mCacheMap.put(file.getPath(), mIcon.get());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Message msg = mHandler.obtainMessage();
                            msg.obj = mIcon.get();
                            msg.sendToTarget();
                        }
                    });
                } else if (isApkFile(file)) {
                    bitmap = Icons.getAPKIcon(file);
                    mIcon = new SoftReference<>(bitmap);
                    fileItem.setIcon(mIcon.get());
                    mCacheMap.put(file.getPath(), mIcon.get());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Message msg = mHandler.obtainMessage();
                            msg.obj = mIcon.get();
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