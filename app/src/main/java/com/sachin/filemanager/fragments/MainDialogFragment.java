package com.sachin.filemanager.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.sachin.filemanager.R;
import com.sachin.filemanager.activities.MainActivity;
import com.sachin.filemanager.utils.FileManagerUtils;
import com.sachin.filemanager.utils.FileUtils;
import com.sachin.filemanager.utils.MainActivityHelper;

import java.io.File;

public class MainDialogFragment extends DialogFragment {
    public static final int PICK_SD_PATH_CODE = 122;

    public static final int CREATE_FOLDER_DIALOG = 1;
    public static final int SORT_TYPE_DIALOG = 2;
    public static final int SAF_TYPE_DIALOG = 3;

    private File file;
    private int sortType;

    public static MainDialogFragment newInstance(int dialogType) {
        MainDialogFragment fragment = new MainDialogFragment();
        Bundle args = new Bundle();
        args.putInt("type", dialogType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int type = getArguments().getInt("type");

        switch (type) {
            case CREATE_FOLDER_DIALOG:
                return makeCreateFolderDialog();
            case SORT_TYPE_DIALOG:
                return makeSortTypeDialog();
            case SAF_TYPE_DIALOG:
                return makeSAFDialog();
            default:
                return super.onCreateDialog(savedInstanceState);
        }

    }

    private Dialog makeSAFDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Write Access Required");
        final View view = View.inflate(getActivity(), R.layout.saf_dialog_layout, null);
        builder.setView(view);
        builder.setPositiveButton("Open", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                getActivity().startActivityForResult(intent, PICK_SD_PATH_CODE);
            }
        });

        builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }

    private Dialog makeSortTypeDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        sortType = FileManagerUtils.getInstance().getSortType();
        final String[] sortItems = getActivity().getResources().getStringArray(R.array.sort_types);
        dialog.setSingleChoiceItems(sortItems, sortType, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sortType = i;
            }
        });
        dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (FileManagerUtils.getInstance().getSortType() != sortType)
                    ((MainActivity) getActivity()).getHelper().updateSortSettings(sortType);

                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = dialog.create();
        return alertDialog;
    }

    private Dialog makeCreateFolderDialog() {
        final FileManagerUtils managerUtils = FileManagerUtils.getInstance();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = View.inflate(getActivity(), R.layout.input_layout, null);
        builder.setView(view);
        builder.setTitle(R.string.myfiles_create_folder_dialog_title);
        final EditText editText = (EditText) view.findViewById(R.id.input_field);
        final TextInputLayout textInputLayout = (TextInputLayout) view.findViewById(R.id.textInput);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File target = new File(managerUtils.getCurrentDirectory(), editText.getText().toString());
                //boolean result = managerUtils.createDir(managerUtils.getCurrentDirectory(), editText.getText().toString());
                boolean result = FileUtils.mkdir(target);
                MainActivityHelper helper = ((MainActivity) getActivity()).getHelper();
                View snackView = helper.getRecyclerView();
                if (result) {
                    Snackbar.make(snackView, editText.getText() + " created successfully!", Snackbar.LENGTH_SHORT).show();
                    helper.updateDirectory(managerUtils.getNextDirectory(managerUtils.getCurrentDirectory(), true));

                } else
                    Snackbar.make(snackView, "Can't create folder!", Snackbar.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        editText.getText().append("New Folder");
        editText.selectAll();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean valid = FileUtils.isValidName(s.toString());
                Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                boolean enabled = positive.isEnabled();

                if (s.toString().isEmpty() && enabled)
                    positive.setEnabled(false);

                if (!valid) {
                    textInputLayout.setError("This name is not valid");

                    if (enabled)
                        positive.setEnabled(false);
                } else {
                    if (!enabled)
                        positive.setEnabled(true);

                    if (textInputLayout.isErrorEnabled())
                        textInputLayout.setErrorEnabled(false);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editText.requestFocus();

        return dialog;
    }

}
