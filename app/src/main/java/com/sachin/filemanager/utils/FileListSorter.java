package com.sachin.filemanager.utils;


import com.sachin.filemanager.ui.FileItem;

import java.util.Comparator;

public class FileListSorter implements Comparator<FileItem> {
    public static final int SORT_BY_NAME = 0;
    public static final int SORT_BY_TYPE = 1;
    public static final int SORT_BY_SIZE = 2;
    public static final int SORT_BY_DATE = 3;

    private int directoriesOnTop = 0; // 0 = true
    private int ascending = 1;
    private int sort = 0;

    public FileListSorter(int directoriesOnTop, int ascending, int sort) {
        this.directoriesOnTop = directoriesOnTop;
        this.ascending = ascending;
        this.sort = sort;
    }

    private boolean isDirectory(FileItem item) {
        return item.isDirectory();
    }

    @Override
    public int compare(FileItem fileItem1, FileItem fileItem2) {

        if (directoriesOnTop == 0) {

            if (isDirectory(fileItem1) && !isDirectory(fileItem2))
                return -1;

            else if (isDirectory(fileItem2) && !isDirectory(fileItem1))
                return 1;

        } else if (directoriesOnTop == 1) {

            if (isDirectory(fileItem1) && !isDirectory(fileItem2))
                return 1;

            else if (isDirectory(fileItem2) && !isDirectory(fileItem1))
                return -1;
        }

        if (sort == SORT_BY_NAME)
            // sort by name
            return ascending * fileItem1.getName().compareToIgnoreCase(fileItem2.getName());

        else if (sort == SORT_BY_DATE)
            // sort by last modified
            return ascending * Long.valueOf(fileItem1.getLongDate()).compareTo(fileItem2.getLongDate());

        else if (sort == SORT_BY_SIZE) {

            // sort by size
            if (!fileItem1.isDirectory() && !fileItem2.isDirectory())
                return ascending * Long.valueOf(fileItem1.getLongSize()).compareTo(fileItem2.getLongSize());
            else
                return fileItem1.getName().compareToIgnoreCase(fileItem2.getName());

        } else if (sort == SORT_BY_TYPE) {

            // sort by type
            if (!fileItem1.isDirectory() && !fileItem2.isDirectory()) {

                final String ext_a = getExtension(fileItem1.getName());
                final String ext_b = getExtension(fileItem2.getName());

                final int res = ascending * ext_a.compareTo(ext_b);
                if (res == 0) {
                    return ascending * fileItem1.getName().compareToIgnoreCase(fileItem2.getName());
                }
                return res;
            } else {
                return fileItem1.getName().compareToIgnoreCase(fileItem2.getName());
            }
        }
        return 0;
    }

    private static String getExtension(String a) {
        return a.substring(a.lastIndexOf(".") + 1).toLowerCase();
    }
}
