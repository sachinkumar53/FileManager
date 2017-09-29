package com.sachin.filemanager.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import io.codetail.animation.ViewAnimationUtils;

import static android.view.View.TRANSLATION_X;

public class AnimationUtils {

    private static boolean toolbarVisible = false;

    public static void animateToToolBar(final View fab, final View toolbar) {

        final int cx = toolbar.getWidth() / 2;
        final int cy = toolbar.getHeight() / 2;

        final float startRadius = (fab.getWidth() + fab.getHeight()) / 2;

        final float endRadius = (float) Math.hypot(cx, cy);

        int x = (-cx + 100);
        int y = 75;

        fab.animate().translationX(x).translationY(y).setInterpolator(new AccelerateInterpolator()).
                setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        toolbar.setVisibility(View.VISIBLE);
                        fab.setVisibility(View.INVISIBLE);
                        Animator reveal = ViewAnimationUtils.createCircularReveal(toolbar, cx, cy, startRadius, endRadius);
                        reveal.setInterpolator(new AccelerateDecelerateInterpolator());
                        reveal.start();

                        toolbarVisible = true;
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }

    public static boolean isToolbarVisible() {
        return toolbarVisible;
    }

    public static void animateToFAB(final View fab, final View toolbar) {

        final int cx = toolbar.getWidth() / 2;
        final int cy = toolbar.getHeight() / 2;

        final float startRadius = (float) Math.hypot(cx, cy);
        final float endRadius = (fab.getWidth() + fab.getHeight()) / 2;

        Animator reveal = ViewAnimationUtils.createCircularReveal(toolbar, cx, cy, startRadius, endRadius);
        reveal.setInterpolator(new AccelerateDecelerateInterpolator());
        reveal.start();

        reveal.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                fab.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.INVISIBLE);
                fab.animate().translationX(0).translationY(0).setInterpolator(new AccelerateInterpolator()).setListener(null);

                toolbarVisible = false;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

    }

    public static Animator animatePath(TextView text, ImageView icon, boolean forward) {
        float startScale = 0f;
        float endScale = 1f;
        int fromX = 300;
        int toX = 0;
        int fromAlpha = 0;
        int toAlPha = 1;

        if (!forward) {
            startScale = 1f;
            endScale = 0f;
            fromX = 0;
            toX = 300;
            fromAlpha = 1;
            toAlPha = 0;
        }

        Animation anim = new ScaleAnimation(startScale, endScale, startScale, endScale, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        anim.setFillAfter(true);
        anim.setDuration(300);
        icon.startAnimation(anim);

        ObjectAnimator animator = ObjectAnimator.ofFloat(text, TRANSLATION_X, fromX, toX);
        animator.setDuration(400);

        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(text, View.ALPHA, fromAlpha, toAlPha);
        animatorAlpha.setDuration(400);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator, animatorAlpha);
        animatorSet.start();

        return animatorSet;
    }
}
