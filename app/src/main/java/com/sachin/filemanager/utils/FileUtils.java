package com.sachin.filemanager.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.sachin.filemanager.FileManager;
import com.sachin.filemanager.FileManagerApplication;
import com.sachin.filemanager.R;
import com.sachin.filemanager.adapters.FileListAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileUtils {

    private static String TAG = "FileUtils";

    private static final String PRIMARY_VOLUME_NAME = "primary";

    /**
     * Hide default constructor.
     */
    private FileUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Copy a file. The target file may even be on external SD card for Kitkat.
     *
     * @param source The source file
     * @param target The target file
     * @return true if the copying was successful.
     */
    @SuppressWarnings("null")
    public static boolean copyFile(@NonNull final File source, @NonNull final File target) {
        FileInputStream inStream = null;
        OutputStream outStream = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inStream = new FileInputStream(source);

            // First try the normal way
            if (isWritable(target)) {
                // standard way
                outStream = new FileOutputStream(target);
                inChannel = inStream.getChannel();
                outChannel = ((FileOutputStream) outStream).getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } else {
                if (SystemUtil.isAndroid5()) {
                    // Storage Access Framework
                    DocumentFile targetDocument = getDocumentFile(target, false, true);
                    if (targetDocument != null) {
                        outStream = FileManagerApplication.getAppContext().getContentResolver().openOutputStream(targetDocument.getUri());
                    }
                } else if (SystemUtil.isKitkat()) {
                    // Workaround for Kitkat ext SD card
                    Uri uri = MediaStoreUtil.getUriFromFile(target.getAbsolutePath());
                    if (uri != null) {
                        outStream = FileManagerApplication.getAppContext().getContentResolver().openOutputStream(uri);
                    }
                } else {
                    return false;
                }

                if (outStream != null) {
                    // Both for SAF and for Kitkat, write to output stream.
                    byte[] buffer = new byte[4096]; // MAGIC_NUMBER
                    int bytesRead;
                    while ((bytesRead = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                }

            }
        } catch (Exception e) {
            Log.e(TAG,
                    "Error when copying file from " + source.getAbsolutePath() + " to " + target.getAbsolutePath(), e);
            return false;
        } finally {
            try {
                inStream.close();
            } catch (Exception e) {
                // ignore exception
            }
            try {
                outStream.close();
            } catch (Exception e) {
                // ignore exception
            }
            try {
                inChannel.close();
            } catch (Exception e) {
                // ignore exception
            }
            try {
                outChannel.close();
            } catch (Exception e) {
                // ignore exception
            }
        }
        return true;
    }

    /**
     * Delete a file. May be even on external SD card.
     *
     * @param file the file to be deleted.
     * @return True if successfully deleted.
     */
    public static boolean deleteFile(@NonNull final File file) {
        // First try the normal deletion.
        if (file.delete()) {
            return true;
        }

        // Try with Storage Access Framework.
        if (SystemUtil.isAndroid5()) {
            DocumentFile document = getDocumentFile(file, false, true);
            return document != null && document.delete();
        }

        // Try the Kitkat workaround.
        if (SystemUtil.isKitkat()) {
            ContentResolver resolver = FileManagerApplication.getAppContext().getContentResolver();

            try {
                Uri uri = MediaStoreUtil.getUriFromFile(file.getAbsolutePath());
                if (uri != null) {
                    resolver.delete(uri, null, null);
                }
                return !file.exists();
            } catch (Exception e) {
                Log.e(TAG, "Error when deleting file " + file.getAbsolutePath(), e);
                return false;
            }
        }

        return !file.exists();
    }

    /**
     * Move a file. The target file may even be on external SD card.
     *
     * @param source The source file
     * @param target The target file
     * @return true if the copying was successful.
     */
    public static boolean moveFile(@NonNull final File source, @NonNull final File target) {
        // First try the normal rename.
        boolean success = source.renameTo(target);

        if (!success) {
            success = copyFile(source, target);

            if (success && target.length() < source.length()) {
                Log.w(TAG, "Lengh reduced from " + source.length() + " to " + target.length()
                        + " while copying file " + source.getName() + ". Trying once more.");
                success = copyFile(source, target);
            }
            if (success) {
                success = deleteFile(source);
            }
        }

        return success;
    }

    /**
     * Rename a folder. In case of extSdCard in Kitkat, the old folder stays in place, but files are moved.
     *
     * @param source The source folder.
     * @param target The target folder.
     * @return true if the renaming was successful.
     */
    public static boolean renameFolder(@NonNull final File source, @NonNull final File target) {
        // First try the normal rename.
        if (source.renameTo(target)) {
            return true;
        }
        if (target.exists()) {
            return false;
        }

        // Try the Storage Access Framework if it is just a rename within the same parent folder.
        if (SystemUtil.isAndroid5() && source.getParent().equals(target.getParent())) {
            DocumentFile document = getDocumentFile(source, true, true);
            if (document != null && document.renameTo(target.getName())) {
                return true;
            }
        }

        // Try the manual way, moving files individually.
        if (!mkdir(target)) {
            return false;
        }

        File[] sourceFiles = source.listFiles();

        if (sourceFiles == null) {
            return true;
        }

        for (File sourceFile : sourceFiles) {
            String fileName = sourceFile.getName();
            File targetFile = new File(target, fileName);
            if (!copyFile(sourceFile, targetFile)) {
                // stop on first error
                return false;
            }
        }
        // Only after successfully copying all files, delete files on source folder.
        for (File sourceFile : sourceFiles) {
            if (!deleteFile(sourceFile)) {
                // stop on first error
                return false;
            }
        }
        return true;
    }

    /**
     * Get a temp file.
     *
     * @param file The base file for which to create a temp file.
     * @return The temp file.
     */
    @NonNull
    public static File getTempFile(@NonNull final File file) {
        File extDir = new File(FileManagerApplication.getAppContext().getExternalCacheDir(), "temp");
        if (!extDir.exists()) {
            // noinspection ResultOfMethodCallIgnored
            extDir.mkdirs();
        }
        return new File(extDir, file.getName());
    }

    /**
     * Create a folder. The folder may even be on external SD card for Kitkat.
     *
     * @param file The folder to be created.
     * @return True if creation was successful.
     */
    public static boolean mkdir(@NonNull final File file) {
        if (file.exists()) {
            // nothing to create.
            return file.isDirectory();
        }

        // Try the normal way
        if (file.mkdir()) {
            return true;
        }

        // Try with Storage Access Framework.
        if (SystemUtil.isAndroid5()) {
            DocumentFile document = getDocumentFile(file, true, true);
            // getDocumentFile implicitly creates the directory.
            return document != null && document.exists();
        }

        // Try the Kitkat workaround.
        if (SystemUtil.isKitkat()) {
            File tempFile = new File(file, "dummyImage.jpg");

            File dummySong = copyDummyFiles();
            if (dummySong == null) {
                return false;
            }
            int albumId = MediaStoreUtil.getAlbumIdFromAudioFile(dummySong);
            Uri albumArtUri = Uri.parse("content://media/external/audio/albumart/" + albumId);

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DATA, tempFile.getAbsolutePath());
            contentValues.put(MediaStore.Audio.AlbumColumns.ALBUM_ID, albumId);

            ContentResolver resolver = FileManagerApplication.getAppContext().getContentResolver();
            if (resolver.update(albumArtUri, contentValues, null, null) == 0) {
                resolver.insert(Uri.parse("content://media/external/audio/albumart"), contentValues);
            }
            try {
                ParcelFileDescriptor fd = resolver.openFileDescriptor(albumArtUri, "r");
                if (fd != null) {
                    fd.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Could not open file", e);
                return false;
            } finally {
                FileUtils.deleteFile(tempFile);
            }

            return true;
        }

        return false;
    }

    /**
     * Delete a folder.
     *
     * @param file The folder name.
     * @return true if successful.
     */
    public static boolean rmdir(@NonNull final File file) {
        if (!file.exists()) {
            return true;
        }
        if (!file.isDirectory()) {
            return false;
        }
        String[] fileList = file.list();
        if (fileList != null && fileList.length > 0) {
            // Delete only empty folder.
            return false;
        }

        // Try the normal way
        if (file.delete()) {
            return true;
        }

        // Try with Storage Access Framework.
        if (SystemUtil.isAndroid5()) {
            DocumentFile document = getDocumentFile(file, true, true);
            return document != null && document.delete();
        }

        // Try the Kitkat workaround.
        if (SystemUtil.isKitkat()) {
            ContentResolver resolver = FileManagerApplication.getAppContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            // Delete the created entry, such that content provider will delete the file.
            resolver.delete(MediaStore.Files.getContentUri("external"), MediaStore.MediaColumns.DATA + "=?",
                    new String[]{file.getAbsolutePath()});
        }

        return !file.exists();
    }

    /**
     * Delete all files in a folder.
     *
     * @param folder the folder
     * @return true if successful.
     */
    public static boolean deleteFilesInFolder(@NonNull final File folder) {
        boolean totalSuccess = true;

        String[] children = folder.list();
        if (children != null) {
            for (String child : children) {
                File file = new File(folder, child);
                if (!file.isDirectory()) {
                    boolean success = FileUtils.deleteFile(file);
                    if (!success) {
                        Log.w(TAG, "Failed to delete file" + child);
                        totalSuccess = false;
                    }
                }
            }
        }
        return totalSuccess;
    }

    /**
     * Delete a directory asynchronously.
     *
     * @param activity    The activity calling this method.
     * @param file        The folder name.
     * @param postActions Commands to be executed after success.
     */
    public static void rmdirAsynchronously(@NonNull final Activity activity, @NonNull final File file, final Runnable postActions) {
        new Thread() {
            @Override
            public void run() {
                int retryCounter = 5; // MAGIC_NUMBER
                while (!FileUtils.rmdir(file) && retryCounter > 0) {
                    try {
                        Thread.sleep(100); // MAGIC_NUMBER
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                    retryCounter--;
                }
                if (file.exists()) {
                    //DialogUtil.displayError(activity, R.string.message_dialog_failed_to_delete_folder, false,
                    //file.getAbsolutePath());
                } else {
                    activity.runOnUiThread(postActions);
                }

            }
        }.start();
    }

    /**
     * Check is a file is writable. Detects write issues on external SD card.
     *
     * @param file The file
     * @return true if the file is writable.
     */
    public static boolean isWritable(@NonNull final File file) {
        boolean isExisting = file.exists();

        try {
            FileOutputStream output = new FileOutputStream(file, true);
            try {
                output.close();
            } catch (IOException e) {
                // do nothing.
            }
        } catch (FileNotFoundException e) {
            return false;
        }
        boolean result = file.canWrite();

        // Ensure that file is not created during this process.
        if (!isExisting) {
            // noinspection ResultOfMethodCallIgnored
            file.delete();
        }

        return result;
    }

    // Utility methods for Android 5

    /**
     * Check for a directory if it is possible to create files within this directory, either via normal writing or via
     * Storage Access Framework.
     *
     * @param folder The directory
     * @return true if it is possible to write in this directory.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean isWritableNormalOrSaf(@Nullable final File folder) {
        // Verify that this is a directory.
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return false;
        }

        // Find a non-existing file in this directory.
        int i = 0;
        File file;
        do {
            String fileName = "AugendiagnoseDummyFile" + (++i);
            file = new File(folder, fileName);
        }
        while (file.exists());

        // First check regular writability
        if (isWritable(file)) {
            return true;
        }

        // Next check SAF writability.
        DocumentFile document;
        try {
            document = getDocumentFile(file, false, false);
        } catch (Exception e) {
            return false;
        }

        if (document == null) {
            return false;
        }

        // This should have created the file - otherwise something is wrong with access URL.
        boolean result = document.canWrite() && file.exists();

        // Ensure that the dummy file is not remaining.
        document.delete();

        return result;
    }

    /**
     * Copy the dummy image and dummy mp3 into the private folder, if not yet there. Required for the Kitkat workaround.
     *
     * @return the dummy mp3.
     */
    private static File copyDummyFiles() {
        try {
            copyDummyFile(R.raw.albumart, "mkdirFiles", "albumart.jpg");
            return copyDummyFile(R.raw.silence, "mkdirFiles", "silence.mp3");

        } catch (IOException e) {
            //Log.e(Application.TAG, "Could not copy dummy files.", e);
            return null;
        }
    }

    /**
     * Get the SD card directory.
     *
     * @return The SD card directory.
     */
    @NonNull
    public static String getSdCardPath() {
        String sdCardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

        try {
            sdCardDirectory = new File(sdCardDirectory).getCanonicalPath();
        } catch (IOException ioe) {
            Log.e(TAG, "Could not get SD directory", ioe);
        }
        return sdCardDirectory;
    }

    /**
     * Get a list of external SD card paths. (Kitkat or higher.)
     *
     * @return A list of external SD card paths.
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private static String[] getExtSdCardPaths() {
        List<String> paths = new ArrayList<>();
        for (File file : FileManagerApplication.getAppContext().getExternalFilesDirs("external")) {
            if (file != null && !file.equals(FileManagerApplication.getAppContext().getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w(TAG, "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        return paths.toArray(new String[paths.size()]);
    }

    /**
     * Determine the main folder of the external SD card containing the given file.
     *
     * @param file the file.
     * @return The main folder of the external SD card containing this file, if the file is on an SD card. Otherwise,
     * null is returned.
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public static String getExtSdCardFolder(@NonNull final File file) {
        String[] extSdPaths = getExtSdCardPaths();
        try {
            for (String extSdPath : extSdPaths) {
                if (file.getCanonicalPath().startsWith(extSdPath)) {
                    return extSdPath;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * Determine if a file is on external sd card. (Kitkat or higher.)
     *
     * @param file The file.
     * @return true if on external sd card.
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public static boolean isOnExtSdCard(@NonNull final File file) {
        return getExtSdCardFolder(file) != null;
    }

    /**
     * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If the file is not
     * existing, it is created.
     *
     * @param file              The file.
     * @param isDirectory       flag indicating if the file should be a directory.
     * @param createDirectories flag indicating if intermediate path directories should be created if not existing.
     * @return The DocumentFile
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static DocumentFile getDocumentFile(@NonNull final File file, final boolean isDirectory,
                                                final boolean createDirectories) {

        Uri treeUri = SettingsUtils.getTreeUri();

        String fullPath;
        try {
            fullPath = file.getCanonicalPath();
        } catch (IOException e) {
            return null;
        }

        String baseFolder = null;

        // First try to get the base folder via unofficial StorageVolume API from the URIs.

        String treeBase = getFullPathFromTreeUri(treeUri);
        if (treeBase != null && fullPath.startsWith(treeBase)) {
            baseFolder = treeBase;
        }

        if (baseFolder == null) {
            // Alternatively, take root folder from device and assume that base URI works.
            baseFolder = getExtSdCardFolder(file);
        }

        if (baseFolder == null) {
            return null;
        }

        String relativePath = fullPath.substring(baseFolder.length() + 1);

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(FileManagerApplication.getAppContext(), treeUri);

        String[] parts = relativePath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);

            if (nextDocument == null) {
                if (i < parts.length - 1) {
                    if (createDirectories) {
                        nextDocument = document.createDirectory(parts[i]);
                    } else {
                        return null;
                    }
                } else if (isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                } else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }

        return document;
    }

    /**
     * Get the full path of a document from its tree URI.
     *
     * @param treeUri The tree RI.
     * @return The path (without trailing file separator).
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    private static String getFullPathFromTreeUri(@Nullable final Uri treeUri) {
        if (treeUri == null) {
            return null;
        }
        String volumePath = FileUtils.getVolumePath(FileUtils.getVolumeIdFromTreeUri(treeUri));
        if (volumePath == null) {
            return File.separator;
        }
        if (volumePath.endsWith(File.separator)) {
            volumePath = volumePath.substring(0, volumePath.length() - 1);
        }

        String documentPath = FileUtils.getDocumentPathFromTreeUri(treeUri);
        if (documentPath.endsWith(File.separator)) {
            documentPath = documentPath.substring(0, documentPath.length() - 1);
        }

        if (documentPath.length() > 0) {
            if (documentPath.startsWith(File.separator)) {
                return volumePath + documentPath;
            } else {
                return volumePath + File.separator + documentPath;
            }
        } else {
            return volumePath;
        }
    }

    /**
     * Get the path of a certain volume.
     *
     * @param volumeId The volume id.
     * @return The path.
     */
    private static String getVolumePath(final String volumeId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }

        try {
            StorageManager mStorageManager =
                    (StorageManager) FileManagerApplication.getAppContext().getSystemService(Context.STORAGE_SERVICE);

            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");

            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getUuid = storageVolumeClazz.getMethod("getUuid");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
            Object result = getVolumeList.invoke(mStorageManager);

            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String uuid = (String) getUuid.invoke(storageVolumeElement);
                Boolean primary = (Boolean) isPrimary.invoke(storageVolumeElement);

                // primary volume?
                if (primary && PRIMARY_VOLUME_NAME.equals(volumeId)) {
                    return (String) getPath.invoke(storageVolumeElement);
                }

                // other volumes?
                if (uuid != null) {
                    if (uuid.equals(volumeId)) {
                        return (String) getPath.invoke(storageVolumeElement);
                    }
                }
            }

            // not found.
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Get the volume ID from the tree URI.
     *
     * @param treeUri The tree URI.
     * @return The volume ID.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getVolumeIdFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");

        if (split.length > 0) {
            return split[0];
        } else {
            return null;
        }
    }

    /**
     * Get the document path (relative to volume name) for a tree URI (LOLLIPOP).
     *
     * @param treeUri The tree URI.
     * @return the document path.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getDocumentPathFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if ((split.length >= 2) && (split[1] != null)) {
            return split[1];
        } else {
            return File.separator;
        }
    }

    // Utility methods for Kitkat

    /**
     * Copy a resource file into a private target directory, if the target does not yet exist. Required for the Kitkat
     * workaround.
     *
     * @param resource   The resource file.
     * @param folderName The folder below app folder where the file is copied to.
     * @param targetName The name of the target file.
     * @return the dummy file.
     * @throws IOException thrown if there are issues while copying.
     */
    private static File copyDummyFile(final int resource, final String folderName, @NonNull final String targetName)
            throws IOException {
        File externalFilesDir = FileManagerApplication.getAppContext().getExternalFilesDir(folderName);
        if (externalFilesDir == null) {
            return null;
        }
        File targetFile = new File(externalFilesDir, targetName);

        if (!targetFile.exists()) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = FileManagerApplication.getAppContext().getResources().openRawResource(resource);
                out = new FileOutputStream(targetFile);
                byte[] buffer = new byte[4096]; // MAGIC_NUMBER
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        // do nothing
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        // do nothing
                    }
                }
            }
        }
        return targetFile;
    }


    @Nullable
    public static String getFullPathFromTreeUri(Context context, @Nullable final Uri treeUri) {
        if (treeUri == null) {
            return null;
        }
        String volumePath = getVolumePath(getVolumeIdFromTreeUri(treeUri), context);
        if (volumePath == null) {
            return File.separator;
        }
        if (volumePath.endsWith(File.separator)) {
            volumePath = volumePath.substring(0, volumePath.length() - 1);
        }

        String documentPath = getDocumentPathFromTreeUri(treeUri);
        if (documentPath.endsWith(File.separator)) {
            documentPath = documentPath.substring(0, documentPath.length() - 1);
        }

        if (documentPath.length() > 0) {
            if (documentPath.startsWith(File.separator)) {
                return volumePath + documentPath;
            } else {
                return volumePath + File.separator + documentPath;
            }
        } else {
            return volumePath;
        }
    }


    private static String getVolumePath(final String volumeId, Context con) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }

        try {
            StorageManager mStorageManager =
                    (StorageManager) con.getSystemService(Context.STORAGE_SERVICE);

            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");

            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getUuid = storageVolumeClazz.getMethod("getUuid");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
            Object result = getVolumeList.invoke(mStorageManager);

            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String uuid = (String) getUuid.invoke(storageVolumeElement);
                Boolean primary = (Boolean) isPrimary.invoke(storageVolumeElement);

                // primary volume?
                if (primary && PRIMARY_VOLUME_NAME.equals(volumeId)) {
                    return (String) getPath.invoke(storageVolumeElement);
                }

                // other volumes?
                if (uuid != null) {
                    if (uuid.equals(volumeId)) {
                        return (String) getPath.invoke(storageVolumeElement);
                    }
                }
            }

            // not found.
            return null;
        } catch (Exception ex) {
            return null;
        }
    }


    private static int KB = 1024;
    private static int MB = KB * KB;
    private static int GB = MB * KB;

    private final static int doDelete = 0;
    private final static int doCopy = 1;
    private final static int doMove = 2;
    private final static int doZip = 3;
    private final static int doUnZip = 4;

    private Context context;
    private FileManager manager;
    private FileListAdapter adapter;

    public FileUtils(Context c, FileListAdapter fileListAdapter, FileManager fileManager) {
        context = c;
        manager = fileManager;
        adapter = fileListAdapter;
    }

    public static String calculateSize(File file) {
        String fileSize = "";
        if (file != null) {
            try {
                if (file.isDirectory()) {
                    String[] items_count = file.list();
                    String count;
                    if (items_count.length == 0)
                        count = "empty";
                    else
                        count = String.valueOf(items_count.length) + " items";

                    return count;
                } else {
                    double size = file.length();
                    if (size > GB) {
                        fileSize = String.format("%.2f Gb ", (double) (size / GB));
                        return fileSize;
                    } else if (size < GB && size > MB) {
                        fileSize = String.format("%.2f Mb ", (double) (size / MB));
                        return fileSize;
                    } else if (size < MB && size > KB) {
                        fileSize = String.format("%.2f Kb ", (double) (size / KB));
                        return fileSize;
                    } else {
                        fileSize = String.format("%.2f bytes ", (double) size);
                        return fileSize;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fileSize;
    }

    public static String getFilePermissions(File file) {
        String permission = "-";
        if (file.canRead())
            permission += "r";

        if (file.canWrite())
            permission += "w";

        if (file.canExecute())
            permission += "x";

        return permission;
    }

    public static void openFile(Context context, File file) throws Exception {
        if (file.isFile() && file.canRead()) {
            String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(uri, type);
            PackageManager pm = context.getPackageManager();
            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent);
            }
        }
    }

    public static String TYPE_VIDEO = "vid";
    public static String TYPE_AUDIO = "aud";
    public static String TYPE_IMAGE = "img";
    public static String TYPE_PDF = "pdf";
    public static String TYPE_APK = "apk";
    public static String TYPE_ARCHIVE = "zip";
    public static String TYPE_TEXT = "txt";
    public static String TYPE_PPT = "ppt";
    public static String TYPE_HTML = "htm";
    public static String TYPE_CONFIG = "con";
    public static String TYPE_JAR = "jar";
    public static String TYPE_XML = "xml";
    public static String TYPE_XLS = "xls";
    public static String TYPE_DOCUMENT = "doc";
    public static String TYPE_CONTACT = "vcf";

    public static String identify(String extension) {
        String ext = extension.toLowerCase();
        String type = "";
        if (ext.equals("pdf")) {
            type = TYPE_PDF;
            return type;

        } else if (ext.equals("mp3") || ext.equals("wma") || ext.equals("m4a") || ext.equals("m4p") ||
                ext.equals("ogg") || ext.equals("wav")) {
            type = TYPE_AUDIO;
            return type;

        } else if (ext.equals("apk")) {
            type = TYPE_APK;
            return type;

        } else if (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("gif") ||
                ext.equals("tiff")) {
            type = TYPE_IMAGE;
            return type;

        } else if (ext.equals("zip") || ext.equals("gzip") || ext.equals("gz") || ext.equals("rar")) {
            type = TYPE_ARCHIVE;
            return type;

        } else if (ext.equals("m4v") || ext.equals("avi") || ext.equals("wmv") || ext.equals("3gp") || ext.equals("mp4")) {
            type = TYPE_VIDEO;
            return type;

        } else if (ext.equals("doc") || ext.equals("docx")) {

            type = TYPE_DOCUMENT;
            return type;

        } else if (ext.equals("txt")) {
            type = TYPE_TEXT;
            return type;

        } else if (ext.equals("vcf")) {
            type = TYPE_CONTACT;
            return type;

        } else if (ext.equals("jar")) {
            type = TYPE_JAR;
            return type;

        } else if (ext.equals("conf")) {
            type = TYPE_CONFIG;
            return type;

        } else if (ext.equals("xml")) {
            type = TYPE_XML;
            return type;

        } else if (ext.equals("html")) {
            type = TYPE_HTML;
            return type;

        } else if (ext.equals("ppt") ||
                ext.equals("pptx")) {
            type = TYPE_PPT;
            return type;

        } else if (ext.equals("xls") ||
                ext.equals("xlsx")) {
            type = TYPE_XLS;
            return type;
        } else {
            return type;
        }
    }

    public static boolean isAPKFile(File file) {
        String ext;
        if (file != null && file.isFile()) {
            ext = file.toString();
            String sub_ext = ext.substring(ext.lastIndexOf(".") + 1).toLowerCase();
            return identify(sub_ext).equalsIgnoreCase(TYPE_APK);
        }

        return false;
    }

    public static boolean isImageFile(File file) {
        String ext = file.toString().substring(file.toString().lastIndexOf(".") + 1);

        if (ext.equalsIgnoreCase("png") ||
                ext.equalsIgnoreCase("jpg") ||
                ext.equalsIgnoreCase("jpeg") ||
                ext.equalsIgnoreCase("gif") ||
                ext.equalsIgnoreCase("tiff") ||
                ext.equalsIgnoreCase("tif"))
            return true;

        return false;
    }

    public static String getLastModified(File file) {
        String format = "MMM dd, YYYY, hh:mm";
        return getLastModified(file, format);
    }

    public static String getLastModified(File file, String format) {
        Date date = new Date(file.lastModified());
        String dateModified = new SimpleDateFormat(format).format(date);
        return dateModified;
    }


    public static boolean isValidName(String name) {
        if (name != null && name.length() > 0) {

            if (name.contains("\\") ||
                    name.contains("*") ||
                    name.contains("/") ||
                    name.contains(":") ||
                    name.contains("<") ||
                    name.contains(">") ||
                    name.contains("^") ||
                    name.contains("?") ||
                    name.contains("|") ||
                    name.contains("\""))

                return false;
        }

        return true;
    }

    private class BackgroundWork extends AsyncTask<String, Void, ArrayList<String>> {
        private ProgressDialog progressDialog;
        private int type;

        public BackgroundWork(int workType) {
            type = workType;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(context);

            switch (type) {
                case doDelete:
                    progressDialog.setMessage("Deleting");
                    progressDialog.show();
                    break;
                case doCopy:
                    progressDialog.setMessage("Copying");
                    progressDialog.show();
                    break;
                case doMove:
                    progressDialog.setMessage("Moving");
                    progressDialog.show();
                    break;
            }
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            if (type == doDelete) {
                for (int i = 0; i < params.length; i++)
                    manager.deleteTarget(params[i]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);
            adapter.getFileArrayList().remove(adapter.getFileArrayList().indexOf(strings));
            adapter.notifyDataSetChanged();
            if (progressDialog.isShowing()) {
                progressDialog.cancel();
            }


        }
    }
}
