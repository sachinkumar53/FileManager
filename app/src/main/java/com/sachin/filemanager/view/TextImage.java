package com.sachin.filemanager.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sachin.filemanager.R;
import com.sachin.filemanager.utils.AnimationUtils;
import com.sachin.filemanager.utils.IconUtils;

public class TextImage extends LinearLayout {
    private ImageView imageView;
    private TextView textView;
    private boolean icon;
    private String text;
    int size = 15;
    int color;

    public TextImage(Context context) {
        super(context);
        init(context);
    }

    public TextImage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
        setClickable(true);
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground,value,true);
        setBackground(ContextCompat.getDrawable(context,value.resourceId));
    }

    private void init(Context context) {
        color = IconUtils.getAccentColor();
        imageView = new ImageView(context);
        textView = new TextView(context);
        textView.setTextSize(size);
        textView.setTextColor(color);

        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_chevron_right_white_24dp);
        imageView.setImageDrawable(drawable);
        imageView.setAdjustViewBounds(true);

        addView(imageView);
        addView(textView);

        if (!icon)
            imageView.setVisibility(GONE);

        AnimationUtils.animatePath(textView,imageView,true);
    }

    public void setIcon(boolean icon) {
        this.icon = icon;
        imageView.setVisibility(icon ? VISIBLE : GONE);
    }

    public void setTextColor(int color) {
        this.textView.setTextColor(color);
    }

    public void setTextSize(float size) {
        this.textView.setTextSize(size);
    }

    public void setText(String text) {
        this.text = text;
        this.textView.setText(text);
    }

    public String getText() {
        return text;
    }

    public void goBack(final ViewGroup group) {
        AnimationUtils.animatePath(textView,imageView,false).addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                group.removeView(TextImage.this);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }
}
