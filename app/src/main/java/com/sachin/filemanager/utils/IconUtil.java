package com.sachin.filemanager.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import com.sachin.filemanager.FileManagerApplication;
import com.sachin.filemanager.ui.ThemeUtils;

public class IconUtil {
    private static final Context context = FileManagerApplication.getAppContext();

    public static int getAccentColor() {
        return ThemeUtils.getInstance().getTheme().getColorAccent();
    }

    public static int calculateSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        //Raw width and height of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfWidth = width / 2;
            final int halfHeight = height / 2;

            //Calculate the largest inSampleSize value that is power of 2 and keep both
            //height and width larger than requested height and width.

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static int getThemeInverseColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(
                android.R.attr.textColorPrimary, typedValue, true);
        int color = ContextCompat.getColor(context, typedValue.resourceId);
        return color;
    }

    public static int getAttrColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(
                android.R.attr.windowBackground, typedValue, true);
        int color = ContextCompat.getColor(context, typedValue.resourceId);
        return color;
    }

    public static Rect getRect(int size) {
        return new Rect(size, size, size, size);
    }

    public static Drawable getResizedDrawable(int resId, int iconWidth, int iconHeight) {
        Bitmap BitmapOrg = BitmapFactory.decodeResource(context.getResources(),
                resId);

        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = iconWidth;
        int newHeight = iconHeight;

        // calculate the scale
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);

        return new BitmapDrawable(context.getResources(), resizedBitmap);
    }

    public static int dpToPx(int dpValue) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
        return (int) px;
    }

    public static int pxToDp(int pxValue) {
        float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, pxValue, context.getResources().getDisplayMetrics());
        return (int) dp;
    }
}
