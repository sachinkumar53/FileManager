package com.sachin.filemanager.adapters;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.sachin.filemanager.R;
import com.sachin.filemanager.constants.ThemeColor;
import com.sachin.filemanager.ui.ThemeUtils;

public class ColorGridAdapter extends RecyclerView.Adapter<ColorGridAdapter.ViewHolder> {
    private OnItemClickListener onItemClickListener;

    private int selected = 0;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.color_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        int color = ThemeUtils.getColorFromThemeColor(ThemeColor.values()[position]);

        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
        drawable.getPaint().setColor(color);
        holder.imageButton.setBackground(drawable);
        holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null)
                    onItemClickListener.onItemClick(v, position);
            }
        });

    }

    public void setSelected(int position) {
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        int count = ThemeColor.values().length - 2;
        return count;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageButton imageButton;

        public ViewHolder(View itemView) {
            super(itemView);
            imageButton = (ImageButton) itemView.findViewById(R.id.color_view);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
