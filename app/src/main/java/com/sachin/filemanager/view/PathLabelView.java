package com.sachin.filemanager.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sachin.filemanager.utils.FileManagerUtils;
import com.sachin.filemanager.utils.MainActivityHelper;

import java.util.ArrayList;
import java.util.List;

public class PathLabelView extends HorizontalScrollView implements View.OnClickListener, View.OnLongClickListener {

    private Context context;
    private LinearLayout mainHolder;
    private FileManagerUtils managerUtils;

    private MainActivityHelper helper;

    public PathLabelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public void setHelper(MainActivityHelper helper) {
        this.helper = helper;
    }

    private void init() {
        managerUtils = FileManagerUtils.getInstance();
        fullScroll(SCROLL_AXIS_HORIZONTAL);
        setHorizontalScrollBarEnabled(false);

        mainHolder = new LinearLayout(context);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        mainHolder.setGravity(Gravity.CENTER_VERTICAL);
        addView(mainHolder, params);
        fullScroll(SCROLL_AXIS_HORIZONTAL);
    }

    public void changeVolume(String newVolume) {
        if (mainHolder.getChildCount() > 0)
            mainHolder.removeAllViews();
        updatePathLabel(newVolume,true, false);
    }

    public void updatePathLabel(String next,String tag, boolean isFull, boolean icon) {
        String name = next;
        if (isFull && helper.getPathLabel(next) != null)
            name = helper.getPathLabel(next);

        TextImage textImage = new TextImage(context);
        textImage.setIcon(icon);
        textImage.setText(name);

        if (tag == null)
            textImage.setTag(getFullPath(next,isFull));
        else
            textImage.setTag(tag);

        textImage.setOnClickListener(this);
        mainHolder.addView(textImage);
        requestChildFocus(mainHolder,textImage);
    }

    public void updatePathLabel(String next,boolean isFull,boolean icon){
        updatePathLabel(next,null,isFull,icon);
    }

    public void goToPrevious() {

        int last = mainHolder.getChildCount() - 1;

        if (last <= 0)
            return;

        TextImage textImage = (TextImage) mainHolder.getChildAt(last);
        textImage.goBack(mainHolder);
    }

    @Override
    public void onClick(View view) {

        int index = mainHolder.indexOfChild(view);
        int last = mainHolder.getChildCount() - 1;

        String fullPath = view.getTag().toString();
        final LinearLayout linearLayout = new LinearLayout(context);

        List<TextImage> imageList = new ArrayList<>();

        for (int i = last; i > index; i--) {
            Log.w(getClass().getSimpleName(), "" + i);
            TextImage ti = (TextImage) mainHolder.getChildAt(i);
            imageList.add(ti);
            mainHolder.removeViewAt(i);
        }

        for (int i = (imageList.size() - 1); i > -1; i--) {
            linearLayout.addView(imageList.get(i));
        }

        mainHolder.addView(linearLayout);
        ObjectAnimator animator = ObjectAnimator.ofFloat(linearLayout, TRANSLATION_X, 0, 300);
        animator.setDuration(400);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mainHolder.removeView(linearLayout);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        Log.w(getClass().getSimpleName(), fullPath);

        helper.updateDirectory(managerUtils.jumpToDirectory(fullPath));
    }

    private String getFullPath(String next, boolean isFull) {
        if (isFull) {
            return next;
        }

        String fullPath = managerUtils.getCurrentDirectory();
        return fullPath;
    }

    @Override
    public boolean onLongClick(View v) {
        TextImage textImage = (TextImage)v;
        ClipboardManager clipboardManager = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(textImage.getText(),v.getTag().toString());
        clipboardManager.setPrimaryClip(clip);
        Toast.makeText(getContext(), "Path copied to clipboard", Toast.LENGTH_SHORT).show();
        return true;
    }
}
