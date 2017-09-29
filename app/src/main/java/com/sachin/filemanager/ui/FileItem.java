package com.sachin.filemanager.ui;

import android.graphics.Bitmap;

import java.io.File;

public class FileItem {
    private String name;
    private String path;
    private String date;
    private long longDate;
    private String size;
    private long longSize;
    private String permissions;
    private boolean file;
    private boolean directory;
    private Bitmap icon;

    public FileItem(String name, String path, String date, long dateInLong, String size, long sizeInLong, String permissions,
                    boolean isFile, boolean isDirectory, Bitmap icon) {
        this.name = name;
        this.path = path;
        this.date = date;
        this.longDate = dateInLong;
        this.size = size;
        this.longSize = sizeInLong;
        this.permissions = permissions;
        this.file = isFile;
        this.directory = isDirectory;
        this.icon = icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getDate() {
        return date;
    }

    public long getLongDate() {
        return longDate;
    }

    public String getSize() {
        return size;
    }

    public long getLongSize() {
        return longSize;
    }

    public String getPermissions() {
        return permissions;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public boolean isFile() {
        return file;
    }

    public boolean isDirectory() {
        return directory;
    }

    public File getBaseFile() {
        return new File(path);
    }
}
