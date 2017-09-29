package com.sachin.filemanager.layouts;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.sachin.filemanager.adapters.MainAdapter;


public class MyLinearLayoutManager extends LinearLayoutManager {
    private MainAdapter adapter;

    public MyLinearLayoutManager(Context context, int orientation, boolean reverseLayout, MainAdapter myAdapter) {
        super(context, orientation, reverseLayout);
        this.adapter = myAdapter;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        adapter.setAnimation(false);
    }
}
