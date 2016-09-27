package com.jessfog.dropbox;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jessemartinez on 9/21/16.
 */

public class UploadTask extends AsyncTask {

    private DbxClientV2 dbxClient;
    private File file;
    private Context context;
    private View view;
    private UploadTask.UploadTaskDelegate mDelegate;

    UploadTask(DbxClientV2 dbxClient, File file, Context context, View view) {
        this.dbxClient = dbxClient;
        this.file = file;
        this.context = context;
        this.view = view;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            // Upload to Dropbox
            InputStream inputStream = new FileInputStream(file);
            dbxClient.files().uploadBuilder("/" + file.getName())
                    .withMode(WriteMode.OVERWRITE) //always overwrite existing file
                    .uploadAndFinish(inputStream);
            Log.d("Upload Status", "Success");
        } catch (DbxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        Snackbar snackbar = Snackbar
                .make(view, context.getResources().getString(R.string.upload_success), Snackbar.LENGTH_LONG);
        snackbar.show();
        mDelegate.onUploadTaskComplete();
    }

    public void setDelegate(MainActivity mainActivity) {
        mDelegate = mainActivity;
    }

    public interface UploadTaskDelegate {
        void onUploadTaskComplete();
    }
}