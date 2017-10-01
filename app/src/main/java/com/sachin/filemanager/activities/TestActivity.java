package com.sachin.filemanager.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sachin.filemanager.R;
import com.sachin.filemanager.utils.FileManagerUtils;
import com.sachin.filemanager.utils.FileUtil;

import java.io.File;
import java.util.List;

public class TestActivity extends AppCompatActivity {
    private FileManagerUtils fileManagerUtils;
    private ListView listView;
    private AryanAdapter aryanAdapter;
    private List<String> list;
    private String string;
    private boolean back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        back = false;

        string = Environment.getExternalStorageDirectory().getAbsolutePath();
        fileManagerUtils = new FileManagerUtils();
        //list = new ArrayList<>(fileManagerUtils.setHomeDirectory(string));
        aryanAdapter = new AryanAdapter(this, R.layout.list_item_layout, list);

        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(aryanAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String next = list.get(position);
                File file = new File(fileManagerUtils.getCurrentDirectory(), next);
                if (file.isDirectory()) {
                    if (!list.isEmpty() && list.size() != 0)
                        list.clear();

                    //for (String s : fileManagerUtils.getNextDirectory(next,false))
                      //  list.add(s);

                    aryanAdapter.notifyDataSetChanged();
                    listView.setAdapter(aryanAdapter);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

        if (string.equals(fileManagerUtils.getCurrentDirectory())) {
            if (back)
                super.onBackPressed();
            else {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        back = false;
                    }
                }, 2000);
                back = true;
                Toast.makeText(this, "Press again to exit!", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (!list.isEmpty())
                list.clear();

            //for (String s : fileManagerUtils.getPreviousDirectory())
              //  list.add(s);
            aryanAdapter.notifyDataSetChanged();
            listView.setAdapter(aryanAdapter);
        }
    }

    private class AryanAdapter extends ArrayAdapter<String> {
        private List<String> list;
        private Context c;
        private VH vh;

        public AryanAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<String> strings) {
            super(context, resource, strings);
            list = strings;
            c = context;
        }

        @Nullable
        @Override
        public String getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(c);
                convertView = inflater.inflate(R.layout.list_item_layout, null, false);
                vh = new VH();
                vh.title = (TextView) convertView.findViewById(R.id.my_title);
                vh.summary = (TextView) convertView.findViewById(R.id.my_summary);
                convertView.setTag(vh);
            } else {
                vh = (VH) convertView.getTag();
            }
            File file = new File(fileManagerUtils.getCurrentDirectory(), list.get(position));
            vh.title.setText(file.getName());
            vh.summary.setText(FileUtil.calculateSize(file));
            return convertView;
        }
    }

    private class VH {
        private TextView title;
        private TextView summary;
    }
}
