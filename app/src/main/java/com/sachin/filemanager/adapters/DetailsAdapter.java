package com.sachin.filemanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sachin.filemanager.R;

import java.util.List;


public class DetailsAdapter extends ArrayAdapter<Model> {
    private List<Model> modelList;

    public DetailsAdapter(Context context, int resource, List<Model> models) {
        super(context, resource, models);
        modelList = models;
    }


    @Override
    public Model getItem(int position) {
        return modelList.get(position);
    }

    @Override
    public int getCount() {
        return modelList.size();
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
