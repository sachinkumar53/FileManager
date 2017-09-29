package com.sachin.filemanager.utils;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.util.ArrayList;

public class BackgroundWork extends AsyncTask<String, Void, ArrayList<String>> {
    private ProgressDialog progressDialog;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<String> doInBackground(String... strings) {
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        super.onPostExecute(strings);
    }
}
