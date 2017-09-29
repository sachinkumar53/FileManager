package com.sachin.filemanager.view;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;

public class HideShowScrollListener extends RecyclerView.OnScrollListener {
    private static final int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;
    private FloatingActionButton fab;
    public HideShowScrollListener (FloatingActionButton fab){
        this.fab = fab;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
            onHide();
            controlsVisible = false;
            scrolledDistance = 0;
        } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
            onShow();
            controlsVisible = true;
            scrolledDistance = 0;
        }

        if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
            scrolledDistance += dy;
        }
    }

    public void onHide(){
        fab.hide();
    }

    public void onShow(){
        fab.show();
    }

}
