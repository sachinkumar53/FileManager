package com.sachin.filemanager.view;


import android.animation.ValueAnimator;
import android.content.Context;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.sachin.filemanager.R;
import com.sachin.filemanager.ui.ThemeUtils;
import com.sachin.filemanager.utils.IconUtil;

public class TopSnackbar {

    public static Snackbar makeView(final View view, String message, int duration) {
        Context context = view.getContext();
        final Snackbar snackbar = Snackbar.make(view, message, duration);
        View snackView = snackbar.getView();
        snackView.setBackgroundColor(ThemeUtils.getInstance().getTheme().getColorPrimary());
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackView.getLayoutParams();
        params.gravity = Gravity.TOP;
        params.setAnchorId(R.id.path_label_view);
        params.anchorGravity = Gravity.BOTTOM;
        params.topMargin = 50;
        snackView.setLayoutParams(params);
        TextView textView = snackView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(IconUtil.getThemeInverseColor(context));
        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.md_red_500));

        snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                int bottom = transientBottomBar.getView().getHeight();
                final ValueAnimator valueAnimator = ValueAnimator.ofInt(bottom, 0);
                valueAnimator.setInterpolator(new AccelerateInterpolator());
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        view.setPadding(0, (Integer) animation.getAnimatedValue(), 0, 0);
                    }
                });
                valueAnimator.setDuration(100);
                valueAnimator.start();
            }

            @Override
            public void onShown(Snackbar transientBottomBar) {
                super.onShown(transientBottomBar);
                int top = transientBottomBar.getView().getHeight();
                view.setPadding(0, top, 0, 0);
            }
        });

        return snackbar;
    }
}
