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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.sachin.filemanager.FileManagerApplication;
import com.sachin.filemanager.R;
import com.sachin.filemanager.utils.IconUtils;

import java.io.File;

import static com.sachin.filemanager.utils.FileUtils.TYPE_APK;
import static com.sachin.filemanager.utils.FileUtils.TYPE_ARCHIVE;
import static com.sachin.filemanager.utils.FileUtils.TYPE_AUDIO;
import static com.sachin.filemanager.utils.FileUtils.TYPE_CONFIG;
import static com.sachin.filemanager.utils.FileUtils.TYPE_CONTACT;
import static com.sachin.filemanager.utils.FileUtils.TYPE_DOCUMENT;
import static com.sachin.filemanager.utils.FileUtils.TYPE_HTML;
import static com.sachin.filemanager.utils.FileUtils.TYPE_IMAGE;
import static com.sachin.filemanager.utils.FileUtils.TYPE_JAR;
import static com.sachin.filemanager.utils.FileUtils.TYPE_PDF;
import static com.sachin.filemanager.utils.FileUtils.TYPE_PPT;
import static com.sachin.filemanager.utils.FileUtils.TYPE_VIDEO;
import static com.sachin.filemanager.utils.FileUtils.TYPE_XLS;
import static com.sachin.filemanager.utils.FileUtils.TYPE_XML;
import static com.sachin.filemanager.utils.FileUtils.identify;

public class Icons {
    private static final int iconSizeCircle = 36;
    private static final int iconSizeMain = 24;
    private static final Context context = FileManagerApplication.getAppContext();

    public static void destroyIconCache() {

    }

    public static Bitmap getIcon(File file) {
        Paint paint = new Paint();
        int circleSize = IconUtils.dpToPx(iconSizeCircle);
        int mainSize = IconUtils.dpToPx(iconSizeMain);

        Bitmap circle = Bitmap.createBitmap(circleSize,circleSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circle);

        paint.setColor(IconUtils.getAccentColor());
        paint.setAntiAlias(true);

        RectF rectF = new RectF(new Rect(0, 0, circleSize, circleSize));

        canvas.drawOval(rectF, paint);

        Bitmap icon = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(),
                getIconResId(file)),mainSize,mainSize,true);

        ColorFilter colorFilter = new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        paint.setColorFilter(colorFilter);

        int mini = (circle.getWidth() - icon.getWidth()) / 2;

        canvas.drawBitmap(icon, mini, mini, paint);
        return circle;
    }

    public static Drawable getAPKIcon(File apkFile) {
        Drawable appIcon = null;
        File file = apkFile;
        String sourcePath = file.getPath();
        if (sourcePath.endsWith(".apk")) {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageArchiveInfo(sourcePath, PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                ApplicationInfo appInfo = packageInfo.applicationInfo;
                if (Build.VERSION.SDK_INT >= 8) {
                    appInfo.sourceDir = sourcePath;
                    appInfo.publicSourceDir = sourcePath;
                }
                Drawable icon = appInfo.loadIcon(context.getPackageManager());
                appIcon = icon;
            }
        }

        if (appIcon == null)
            appIcon = ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon);

        return appIcon;
    }

    public static Bitmap getAPKIconBitmap(File file) {
        return ((BitmapDrawable) getAPKIcon(file)).getBitmap();
    }

    public static Bitmap decodeSampleBitmapFromFile(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), options);
        options.inSampleSize = IconUtils.calculateSampleSize(options, 48, 48);
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
                else
                    return R.drawable.ic_file_type_null;
            }
        }

        return R.drawable.ic_file_type_null;
    }

}
