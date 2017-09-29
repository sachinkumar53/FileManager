package com.sachin.filemanager.utils;

import android.graphics.BitmapFactory;

import com.sachin.filemanager.ui.FileItem;
import com.sachin.filemanager.ui.Icons;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileManagerUtils {
    private String currentDirectory;
    private boolean showHidden;
    private ArrayList<FileItem> list;
    private int sortType = 0;

    private static FileManagerUtils instance = null;

    public static synchronized FileManagerUtils getInstance() {
        if (instance == null)
            instance = new FileManagerUtils();
        return instance;
    }

    public FileManagerUtils() {
        list = new ArrayList<>();
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
            list.add(new FileItem("Empty", "", "00", 00, "0.00b", 0, "rw-r-r", true, false, Icons.getIcon(null)));

        Collections.sort(list, new FileListSorter(0, 0, sortType));

        return list;
                /*switch (sortType) {

                    case SORT_BY_NAME:
                        Arrays.sort(objects, ALPHABET);
                        list.clear();
                        for (Object object : objects) {
                            list.add((FileItem) object);
                        }
                        break;

                    case SORT_BY_TYPE:
                        int index = 0;
                        Arrays.sort(objects, TYPE);
                        list.clear();
                        for (Object object : objects) {
                            if (new File(getCurrentDirectory(), (FileItem) object).isDirectory())
                                list.add(index++, (FileItem) object);
                            else
                                list.add((String) object);
                        }
                        break;

                    case SORT_BY_SIZE:
                        int inde = 0;
                        Arrays.sort(objects, SIZE);
                        list.clear();
                        for (Object object : objects) {
                            if (new File(getCurrentDirectory(), (String) object).isDirectory())
                                list.add(inde++, (String) object);
                            else
                                list.add((String) object);
                        }
                        break;

                    case SORT_BY_DATE:
                        int ind = 0;
                        Arrays.sort(objects, DATE_MODIFIED);
                        list.clear();
                        for (Object object : objects) {
                            if (new File(getCurrentDirectory(), (String) object).isDirectory())
                                list.add(ind++, (String) object);
                            else
                                list.add((String) object);
                        }
                        break;

                }
            }
        }*/
    }

    public FileItem getFileItem(File file) {

        return new FileItem(
                file.getName(), file.getPath(),
                FileUtils.getLastModified(file), file.lastModified(),
                FileUtils.calculateSize(file), file.length(),
                FileUtils.getFilePermissions(file),
                file.isFile(), file.isDirectory(), Icons.getIcon(file));
    }

    public void setSortType(int sortType) {
        this.sortType = sortType;
    }

    private Comparator ALPHABET = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    };

    private Comparator SIZE = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            String current = getCurrentDirectory();
            Long file1 = new File(current, o1).length();
            Long file2 = new File(current, o2).length();
            return file1.compareTo(file2);
        }
    };

    private Comparator DATE_MODIFIED = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            String current = getCurrentDirectory();
            Long file1 = new File(current, o1).lastModified();
            Long file2 = new File(current, o2).lastModified();
            return file1.compareTo(file2);
        }
    };

    private Comparator TYPE = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            String ext1;
            String ext2;
            int toReturn;
            try {
                ext1 = o1.substring(o1.lastIndexOf(".") + 1, o1.length()).toLowerCase();
                ext2 = o2.substring(o2.lastIndexOf(".") + 1, o2.length()).toLowerCase();
            } catch (IndexOutOfBoundsException e) {
                return 0;
            }
            toReturn = ext1.compareTo(ext2);
            if (toReturn == 0)
                return o1.toLowerCase().compareTo(o2.toLowerCase());

            return toReturn;
        }
    };

    public boolean createDir(String path, String displayName) {
        if (path.length() < 1 || displayName.length() < 1)
            return false;

        File file = new File(path, displayName);
        if (!file.exists())
            return file.mkdir();
        return false;
    }

    public boolean deleteFile(File file) {
        if (file.exists() && file.canWrite()) {
            if (file.isFile())
                return file.delete();

            else {
                File[] file_list = file.listFiles();

                if (file_list != null) {
                    if (file_list.length == 0)
                        return file.delete();

                    else if (file_list.length > 0) {

                        for (File f : file_list) {
                            if (f.isDirectory())
                                deleteFile(f);

                            else if (f.isFile())
                                f.delete();
                        }

                        if (file.exists())
                            return file.delete();
                    }
                }
            }
        }

        return false;
    }

    public int getSortType() {
        return sortType;
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
