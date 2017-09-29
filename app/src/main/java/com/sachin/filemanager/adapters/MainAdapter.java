package com.sachin.filemanager.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.sachin.filemanager.R;
import com.sachin.filemanager.ui.FileItem;
import com.sachin.filemanager.ui.Icons;
import com.sachin.filemanager.utils.FileManagerUtils;
import com.sachin.filemanager.utils.FileUtils;
import com.sachin.filemanager.utils.IconLoader;
import com.sachin.filemanager.utils.SettingsUtils;
import com.sachin.filemanager.view.SmoothCheckBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {
    public static final int LIST_VIEW = 0;
    public static final int GRID_VIEW = 1;
    private Context c;

    private int type;
    private List<FileItem> fileItemList;
    private HashMap<Integer, String> selectedItems;
    private OnItemClickListener itemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private boolean multiSelectEnabled;
    private OnOptionItemClickListener onOptionItemClickListener;
    private OnSelectionChanged onSelectionChanged;
    private FileManagerUtils fileManagerUtils;
    private int lastPosition = -1;
    private boolean animation;
    private boolean checkAnim;
    private IconLoader iconLoader;

    public MainAdapter(Context c) {
        this.c = c;
        fileItemList = new ArrayList<>();
        fileManagerUtils = FileManagerUtils.getInstance();
        this.animation = true;
        checkAnim = false;
    }

    public void addAll(List<FileItem> list) {
        if (!fileItemList.isEmpty())
            fileItemList.clear();

        for (FileItem item : list)
            fileItemList.add(item);

        notifyDataSetChanged();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(c);
        View view;
        ViewHolder viewHolder;
        switch (getViewType()) {
            case LIST_VIEW:
                view = inflater.inflate(R.layout.row_layout_item_list, parent, false);
                break;
            case GRID_VIEW:
                view = inflater.inflate(R.layout.row_layout_item_grid, parent, false);
                break;
            default:
                view = inflater.inflate(R.layout.row_layout_item_list, parent, false);
                break;
        }

        viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    public void setAnimation(boolean animation) {
        this.animation = animation;
    }

    public void selectItem(int position) {
        //check if already selected
        if (selectedItems.containsKey(position)) {
            selectedItems.remove(position);
        } else {
            String path = getItem(position) + fileManagerUtils.getCurrentDirectory();
            selectedItems.put(position, path);
        }

        if (onSelectionChanged != null)
            onSelectionChanged.selection(getSelectedItemsCount());

        checkAnim = true;
        notifyItemChanged(position);
    }

    public void selectRange(int start, int end, boolean selected) {
        for (int position = start; position <= end; position++) {
            if (selected) {
                String path = getItem(position) + fileManagerUtils.getCurrentDirectory();
                selectedItems.put(position, path);
            } else
                selectedItems.remove(position);
        }

        notifyItemRangeChanged(start, end - start + 1);

        if (onSelectionChanged != null)
            onSelectionChanged.selection(getSelectedItemsCount());
    }

    public int getSelectedItemsCount() {
        return selectedItems.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final FileItem item = getItem(position);

        holder.itemName.setText(item.getName());
        holder.itemDetail.setText(item.getSize());

        if (item.isFile()) {

            if (SettingsUtils.isThumbnailEnabled()) {
                if (iconLoader == null)
                    iconLoader = new IconLoader();

                if (FileUtils.isImageFile(item.getBaseFile()) || FileUtils.isAPKFile(item.getBaseFile())) {

                    Bitmap bitmap = iconLoader.hasLoadedCache(item.getPath());

                    if (bitmap == null) {
                        Handler handler = new Handler(new Handler.Callback() {
                            @Override
                            public boolean handleMessage(Message msg) {
                                notifyDataSetChanged();
                                return true;
                            }
                        });

                        iconLoader.loadIcon(fileItemList, handler);
                        if (!iconLoader.isAlive())
                            iconLoader.start();

                    } else
                        holder.itemIcon.setImageBitmap(bitmap);

                } else
                    holder.itemIcon.setImageBitmap(item.getIcon());
            } else
                holder.itemIcon.setImageBitmap(Icons.getFileIcon(item.getBaseFile()));

        } else
            holder.itemIcon.setImageBitmap(Icons.getFolderIcon());

        if (isMultiSelectEnabled())
            holder.checkBox.setVisibility(View.VISIBLE);
        else
            holder.checkBox.setVisibility(View.GONE);

        if (selectedItems != null && selectedItems.containsKey(position))
            holder.checkBox.setChecked(true, true);

        else
            holder.checkBox.setChecked(false, checkAnim);

        if (position > lastPosition && animation) {
            holder.itemView.clearAnimation();
            Animation anim = android.view.animation.AnimationUtils.loadAnimation(c, R.anim.row_item_fade_up);
            anim.setStartOffset(30 * position);
            holder.itemView.startAnimation(anim);
            lastPosition = position;
        }
    }

    public void stopThumbnailThread() {
        if (iconLoader != null) {
            iconLoader.cancelLoading(true);
            iconLoader = null;
        }
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public boolean onFailedToRecycleView(ViewHolder holder) {
        holder.itemView.clearAnimation();
        return super.onFailedToRecycleView(holder);
    }

    public void refresh() {
        lastPosition = -1;
        setAnimation(true);
        notifyDataSetChanged();
    }

    public int getViewType() {
        return type;
    }

    public void setViewType(int type) {
        this.type = type;
    }

    @Override
    public int getItemCount() {
        return fileItemList.size();
    }

    public FileItem getItem(int position) {
        if (fileItemList.isEmpty())
            return null;

        return fileItemList.get(position);
    }

    public void removeItem(int position) {
        if (!fileItemList.isEmpty()) {
            fileItemList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public List<FileItem> getFileList() {
        return fileItemList;
    }

    public boolean isMultiSelectEnabled() {
        return multiSelectEnabled;
    }

    public void setMultiSelectEnabled(boolean multiSelectEnabled) {
        if (selectedItems == null)
            selectedItems = new HashMap<>();

        this.multiSelectEnabled = multiSelectEnabled;

        if (!multiSelectEnabled) {
            if (!selectedItems.isEmpty())
                selectedItems.clear();
        }
        checkAnim = false;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.itemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setOnOptionItemClickListener(OnOptionItemClickListener onOptionItemClickListener) {
        this.onOptionItemClickListener = onOptionItemClickListener;
    }

    public void setOnSelectionChanged(OnSelectionChanged onSelectionChanged) {
        this.onSelectionChanged = onSelectionChanged;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }

    public interface OnOptionItemClickListener {
        boolean onOptionItemClick(MenuItem menuItem, int position);
    }

    public interface OnSelectionChanged {
        void selection(int count);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView itemName;
        private TextView itemDetail;
        private ImageView itemIcon;
        private SmoothCheckBox checkBox;
        private ImageView itemOptions;

        public ViewHolder(final View itemView) {
            super(itemView);
            itemName = (TextView) itemView.findViewById(R.id.top_view);
            itemDetail = (TextView) itemView.findViewById(R.id.bottom_view);
            itemIcon = (ImageView) itemView.findViewById(R.id.row_image);
            checkBox = (SmoothCheckBox) itemView.findViewById(R.id.select_checkbox);
            itemOptions = (ImageView) itemView.findViewById(R.id.more_file_options);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null)
                        itemClickListener.onItemClick(v, getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null)
                        return onItemLongClickListener.onItemLongClick(v, getAdapterPosition());
                    return false;
                }
            });

            itemOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PopupMenu popupMenu = new PopupMenu(c, v);
                    popupMenu.getMenuInflater().inflate(R.menu.file_options_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (onOptionItemClickListener != null)
                                return onOptionItemClickListener.onOptionItemClick(item, getAdapterPosition());
                            else
                                return false;
                        }
                    });
                    popupMenu.show();
                }
            });

        }
    }
}
