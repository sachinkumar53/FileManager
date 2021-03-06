package com.sachin.filemanager.ui;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

import com.sachin.filemanager.FileManagerApplication;
import com.sachin.filemanager.R;
import com.sachin.filemanager.utils.IconUtil;
import com.sachin.filemanager.utils.MimeTypes;

import java.io.File;
import java.util.HashMap;

import static com.sachin.filemanager.utils.FileUtil.TYPE_APK;
import static com.sachin.filemanager.utils.FileUtil.TYPE_ARCHIVE;
import static com.sachin.filemanager.utils.FileUtil.TYPE_AUDIO;
import static com.sachin.filemanager.utils.FileUtil.TYPE_CONFIG;
import static com.sachin.filemanager.utils.FileUtil.TYPE_CONTACT;
import static com.sachin.filemanager.utils.FileUtil.TYPE_DOCUMENT;
import static com.sachin.filemanager.utils.FileUtil.TYPE_HTML;
import static com.sachin.filemanager.utils.FileUtil.TYPE_IMAGE;
import static com.sachin.filemanager.utils.FileUtil.TYPE_JAR;
import static com.sachin.filemanager.utils.FileUtil.TYPE_PDF;
import static com.sachin.filemanager.utils.FileUtil.TYPE_PPT;
import static com.sachin.filemanager.utils.FileUtil.TYPE_TEXT;
import static com.sachin.filemanager.utils.FileUtil.TYPE_VIDEO;
import static com.sachin.filemanager.utils.FileUtil.TYPE_XLS;
import static com.sachin.filemanager.utils.FileUtil.TYPE_XML;
import static com.sachin.filemanager.utils.FileUtil.identify;

public class Icons {
    private static final int iconSizeCircle = 36;
    private static final int iconSizeMain = 24;
    private static final Context context = FileManagerApplication.getAppContext();
    private static final String FOLDER = "folder";
    private static HashMap<String, Bitmap> iconCache;

    public static Bitmap getFolderIcon() {
        if (iconCache == null)
            iconCache = new HashMap<>();

        if (iconCache.get(FOLDER) != null)
            return iconCache.get(FOLDER);

        int size = IconUtil.dpToPx(iconSizeCircle);

        Paint paint = new Paint();
        int accent = IconUtil.getAccentColor();
        paint.setColor(accent);
        ColorFilter colorFilter = new PorterDuffColorFilter(accent, PorterDuff.Mode.SRC_IN);
        paint.setColorFilter(colorFilter);

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Bitmap folder = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(),
                R.drawable.icon_folder), size, size, true);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(folder, 0, 0, paint);
        iconCache.put(FOLDER, bitmap);

        return bitmap;
    }

    public static void clearIconCache() {
        if (iconCache != null && !iconCache.isEmpty()) {
            iconCache.clear();
            iconCache = null;
        }
    }

    public static Bitmap getFileIcon(File file) {

        String extension = "";

        if (file != null)
            extension = MimeTypes.getExtension(file.getName());

        if (iconCache == null)
            iconCache = new HashMap<>();

        if (iconCache.containsKey(extension)) {
            Log.w("ICONS", " Cache has value. returned successfully");
            return iconCache.get(extension);
        }

        Paint paint = new Paint();
        int circleSize = IconUtil.dpToPx(iconSizeCircle);
        int mainSize = IconUtil.dpToPx(iconSizeMain);

        Bitmap circle = Bitmap.createBitmap(circleSize, circleSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circle);

        paint.setColor(IconUtil.getAccentColor());
        paint.setAntiAlias(true);

        RectF rectF = new RectF(new Rect(0, 0, circleSize, circleSize));

        canvas.drawOval(rectF, paint);

        Bitmap icon = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(),
                getIconResId(file)), mainSize, mainSize, true);

        ColorFilter colorFilter = new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        paint.setColorFilter(colorFilter);

        int mini = (circle.getWidth() - icon.getWidth()) / 2;

        canvas.drawBitmap(icon, mini, mini, paint);

        iconCache.put(extension, circle);
        Log.w("ICONS", " Cache was null. Added successfully");
        return circle;
    }

    public static Bitmap getAPKIcon(File apkFile) {
        try {
            File file = apkFile;
            String sourcePath = file.getPath();
            if (sourcePath.endsWith(".apk")) {
                PackageManager pm = context.getPackageManager();
                PackageInfo packageInfo = pm.getPackageArchiveInfo(sourcePath, PackageManager.GET_ACTIVITIES);
                ApplicationInfo appInfo = packageInfo.applicationInfo;
                appInfo.sourceDir = sourcePath;
                appInfo.publicSourceDir = sourcePath;
                Drawable icon = appInfo.loadIcon(pm);
                //convert drawable to bitmap
                Bitmap bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                icon.draw(canvas);
                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.sym_def_app_icon);
            return bitmap;
        }

        return null;
    }

    public static Bitmap decodeSampleBitmapFromFile(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), options);
        options.inSampleSize = IconUtil.calculateSampleSize(options, 48, 48);
        options.inJustDecodeBounds = false;

        Bitmap source = BitmapFactory.decodeFile(file.getPath(), options);

        int width = Math.min(source.getWidth(), source.getHeight());
        Bitmap bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Rect rect = new Rect(0, 0, width, width);
        RectF rectF = new RectF(rect);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        float left = (width - source.getWidth()) / 2;
        float top = (width - source.getHeight()) / 2;
        canvas.drawBitmap(source, left, top, paint);
        source.recycle();
        return bitmap;
    }

    private static int getIconResId(File file) {
        String ext;

        if (file != null) {
            if (file.isFile()) {
                ext = file.toString();
                String sub_ext = ext.substring(ext.lastIndexOf(".") + 1).toLowerCase();

                if (identify(sub_ext).equals(TYPE_AUDIO))
                    return R.drawable.ic_audiotrack_black_24dp;

                else if (identify(sub_ext).equals(TYPE_VIDEO))
                    return R.drawable.ic_file_type_vid;

                else if (identify(sub_ext).equals(TYPE_IMAGE))
                    return R.drawable.ic_file_type_img;

                else if (identify(sub_ext).equals(TYPE_APK))
                    return R.drawable.ic_android_black_24dp;

                else if (identify(sub_ext).equals(TYPE_JAR))
                    return R.drawable.ic_file_type_jar;

                else if (identify(sub_ext).equals(TYPE_DOCUMENT))
                    return R.drawable.ic_file_type_txt;

                else if (identify(sub_ext).equals(TYPE_CONTACT))
                    return R.drawable.ic_vcard_black_24dp;

                else if (identify(sub_ext).equals(TYPE_XML))
                    return R.drawable.ic_file_type_xml;

                else if (identify(sub_ext).equals(TYPE_XLS))
                    return R.drawable.ic_file_type_xls;
                else if (identify(sub_ext).equals(TYPE_HTML))
                    return R.drawable.ic_file_type_htm;

                else if (identify(sub_ext).equals(TYPE_PDF))
                    return R.drawable.ic_file_type_pdf;

                else if (identify(sub_ext).equals(TYPE_PPT))
                    return R.drawable.ic_file_type_ppt;

                else if (identify(sub_ext).equals(TYPE_ARCHIVE))
                    return R.drawable.zip;

                else if (identify(sub_ext).equals(TYPE_CONFIG))
                    return R.drawable.icon_folder;

                else if (identify(sub_ext).equals(TYPE_TEXT))
                    return R.drawable.ic_file_type_txt;

                else
                    return R.drawable.ic_file_type_null;
            }
        }

        return R.drawable.ic_file_type_null;
    }

    private Bitmap getVideoDrawable(String path) throws OutOfMemoryError {

        try {
            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path,
                    MediaStore.Images.Thumbnails.MINI_KIND);
            return thumb;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
