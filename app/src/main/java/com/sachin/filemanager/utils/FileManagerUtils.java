package com.sachin.filemanager.utils;

import com.sachin.filemanager.ui.FileItem;
import com.sachin.filemanager.ui.Icons;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileManagerUtils {
    private static FileManagerUtils instance = null;
    private String currentDirectory;
    private boolean showHidden;
    private ArrayList<FileItem> list;
    private int directoriesOnTop = 0;
    private int sortType = 0;
    private int ascending = 1;

    public FileManagerUtils() {
        list = new ArrayList<>();
    }

    public static synchronized FileManagerUtils getInstance() {
        if (instance == null)
            instance = new FileManagerUtils();
        return instance;
    }

    public List<FileItem> setHomeDirectory(String directory) {
        if (currentDirectory == null || !currentDirectory.isEmpty())
            currentDirectory = "";

        currentDirectory += directory;
        File file = new File(currentDirectory);

        return addItemsToList(file);
    }

    public void setShowHidden(boolean showHidden) {
        this.showHidden = showHidden;
    }

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    public List<FileItem> getNextDirectory(String nextItem, boolean fullPath) {
        File file;
        if (fullPath)
            file = new File(nextItem);
        else
            file = new File(getCurrentDirectory(), nextItem);

        if (file != null && file.isDirectory())
            currentDirectory = file.getPath();

        return addItemsToList(file);
    }

    public ArrayList<FileItem> jumpToDirectory(String directory) {
        File file = new File(directory);
        currentDirectory = file.getPath();
        return addItemsToList(file);
    }

    public List<FileItem> getPreviousDirectory() {
        String[] dirs = currentDirectory.split("/");
        int lastIndex = dirs.length - 1;
        String previousItem = dirs[lastIndex];
        int index = currentDirectory.lastIndexOf(previousItem);

        String path = currentDirectory.substring(0, index);
        File file = new File(path);
        if (file != null)
            currentDirectory = file.getPath();
        return addItemsToList(file);
    }

    private ArrayList<FileItem> addItemsToList(File source) {
        //reset the list
        if (!list.isEmpty())
            list.clear();

        if (source != null && source.exists() && source.canRead()) {
            File[] files = source.listFiles();
            int length = files.length;

            if (length > 0) {
                for (File file : files) {
                    if (!showHidden) {
                        if (!file.getName().startsWith("."))
                            list.add(getFileItem(file));

                    } else
                        list.add(getFileItem(file));

                }
            }
        } else
            list.add(new FileItem("Empty", "", "00", 00, "0.00b", 0, "rw-r-r", true, false, Icons.getFileIcon(null)));

        Collections.sort(list, new FileListSorter(directoriesOnTop, ascending, sortType));

        return list;
    }

    public FileItem getFileItem(File file) {
        if (file.isFile())
            return new FileItem(
                    file.getName(), file.getPath(),
                    FileUtil.getLastModified(file), file.lastModified(),
                    FileUtil.calculateSize(file), file.length(),
                    FileUtil.getFilePermissions(file),
                    file.isFile(), file.isDirectory(), Icons.getFileIcon(file));

        return new FileItem(
                file.getName(), file.getPath(),
                FileUtil.getLastModified(file), file.lastModified(),
                FileUtil.calculateSize(file), file.length(),
                FileUtil.getFilePermissions(file),
                file.isFile(), file.isDirectory(), Icons.getFolderIcon());
    }

    public int getSortType() {
        return sortType;
    }

    public void setSortType(int sortType) {
        this.sortType = sortType;
    }

    public void showDirectoriesOnTop(boolean top) {
        this.directoriesOnTop = top ? 0:1;
    }

    public boolean isDirectoriesOnTop() {
        return directoriesOnTop == 0;
    }

    public int getAscending() {
        return ascending;
    }

    public void setAscending(int ascending) {
        this.ascending = ascending;
    }

    public void searchForFile(File dir, String name) {
        File[] listFile;
        listFile = dir.listFiles();

        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].isDirectory()) {
                    searchForFile(listFile[i], name);
                } else {
                    if (listFile[i].getName().toLowerCase().equals(name)) {
                        //files_list.add(listFile[i]);
                    }
                }
            }
        }
    }
}
