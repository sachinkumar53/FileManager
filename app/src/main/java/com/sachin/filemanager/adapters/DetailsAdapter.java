package com.sachin.filemanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sachin.filemanager.R;
import com.sachin.filemanager.ui.ListItem;

import java.util.List;


public class DetailsAdapter extends ArrayAdapter<ListItem> {
    private List<ListItem> listItemList;

    public DetailsAdapter(Context context, int resource, List<ListItem> listItems) {
        super(context, resource, listItems);
        listItemList = listItems;
    }


    @Override
    public ListItem getItem(int position) {
        return listItemList.get(position);
    }

    @Override
    public int getCount() {
        return listItemList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.list_item_layout,parent,false);
        }

        ViewHolder viewHolder = new ViewHolder(convertView);
        viewHolder.header.setText(getItem(position).getHeading());
        viewHolder.content.setText(getItem(position).getContent());
        return convertView;
    }


    private class ViewHolder {
        TextView header;
        TextView content;

        public ViewHolder(View itemView) {
            header = (TextView) itemView.findViewById(R.id.my_title);
            content = (TextView) itemView.findViewById(R.id.my_summary);
        }
    }
}
