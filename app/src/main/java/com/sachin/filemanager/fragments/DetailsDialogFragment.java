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
import com.sachin.filemanager.adapters.Model;
import com.sachin.filemanager.constants.KEYS;
import com.sachin.filemanager.utils.FileUtils;
import com.sachin.filemanager.utils.SettingsUtils;

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
        builder.setPositiveButton("Ascending", this);
        builder.setNegativeButton("Descending", this);
        builder.setCancelable(false);

        ListView listView = (ListView) view.findViewById(R.id.details_lv);

        List<Model> models = getList(file);

        adapter = new DetailsAdapter(getActivity(), R.layout.list_item_layout, models);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == AlertDialog.BUTTON_POSITIVE)
            SettingsUtils.applySettings(KEYS.PREFS_SORT_ASCENDING, 0);

        else if (which == AlertDialog.BUTTON_NEGATIVE)
            SettingsUtils.applySettings(KEYS.PREFS_SORT_ASCENDING, 1);
    }

    public List<Model> getList(File file) {
        List<Model> models = new ArrayList<>();

        models.add(new Model("Name", file.getName()));
        models.add(new Model("Path", file.getAbsolutePath()));
        models.add(new Model("Size", FileUtils.calculateSize(file)));
        models.add(new Model("Date modified", FileUtils.getLastModified(file)));
        return models;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
        Model model = adapter.getItem(position);
        ClipData clip = ClipData.newPlainText(model.getHeading(), model.getContent());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), model.getHeading() + " copied to clipboard", Toast.LENGTH_SHORT).show();
        return true;
    }
}
