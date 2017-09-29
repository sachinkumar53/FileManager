package com.sachin.filemanager.adapters;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.sachin.filemanager.R;

public class ThemePagerAdapter extends PagerAdapter{
    String[] titles = {"Primary", "Accent"};
    private Context context;
    private LayoutInflater inflater;
    private RecyclerView recyclerView;
    private ColorGridAdapter gridAdapter;
    private ColorGridAdapter.OnItemClickListener onItemClickListener;
    private int colorSelected = 0;

    public ThemePagerAdapter(Context context) {
        this.context = context;
        gridAdapter = new ColorGridAdapter();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.theme_dialog_page, container,false);

        recyclerView = (RecyclerView)view.findViewById(R.id.color_grid);
        recyclerView.setLayoutManager(new GridLayoutManager(context,4));
        gridAdapter.setSelected(colorSelected);
        recyclerView.setAdapter(gridAdapter);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    public void setOnItemClickListener(ColorGridAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        gridAdapter.setOnItemClickListener(onItemClickListener);
    }

    public void setColorSelected(int position) {
        this.colorSelected = position;
        gridAdapter.setSelected(position);
    }
}
