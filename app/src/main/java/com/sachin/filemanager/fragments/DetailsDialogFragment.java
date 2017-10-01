package com.sachin.filemanager.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.sachin.filemanager.R;
import com.sachin.filemanager.adapters.DetailsAdapter;
import com.sachin.filemanager.ui.ListItem;
import com.sachin.filemanager.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.CLIPBOARD_SERVICE;

public class DetailsDialogFragment extends DialogFragment implements DialogInterface.OnClickListener, AdapterView.OnItemLongClickListener {
    private DetailsAdapter adapter;

    public static DetailsDialogFragment newInstance(String filePath) {
        DetailsDialogFragment fragment = new DetailsDialogFragment();
        Bundle args = new Bundle();
        args.putString("filePath", filePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String filePath = getArguments().getString("filePath");
        File file = new File(filePath);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.details_dialog_layout, null);
        builder.setView(view);
        builder.setTitle("File Properties");
        builder.setPositiveButton(android.R.string.ok,this);
        builder.setCancelable(false);

        ListView listView = (ListView) view.findViewById(R.id.details_lv);

        List<ListItem> listItems = getList(file);

        adapter = new DetailsAdapter(getActivity(), R.layout.list_item_layout, listItems);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dismiss();
    }

    public List<ListItem> getList(File file) {
        List<ListItem> listItems = new ArrayList<>();

        listItems.add(new ListItem("Name", file.getName()));
        listItems.add(new ListItem("Path", file.getAbsolutePath()));
        listItems.add(new ListItem("Size", FileUtil.calculateSize(file)));
        listItems.add(new ListItem("Date modified", FileUtil.getLastModified(file)));
        return listItems;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
        ListItem listItem = adapter.getItem(position);
        ClipData clip = ClipData.newPlainText(listItem.getHeading(), listItem.getContent());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), listItem.getHeading() + " copied to clipboard", Toast.LENGTH_SHORT).show();
        return true;
    }
}
