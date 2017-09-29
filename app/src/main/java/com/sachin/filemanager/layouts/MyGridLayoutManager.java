package com.sachin.filemanager.layouts;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.sachin.filemanager.adapters.MainAdapter;


public class MyGridLayoutManager extends GridLayoutManager {
    private MainAdapter adapter;

    public MyGridLayoutManager(Context context, int spanCount, MainAdapter myAdapter) {
        super(context, spanCount);
        this.adapter = myAdapter;
    }


    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        adapter.setAnimation(false);
    }
}
