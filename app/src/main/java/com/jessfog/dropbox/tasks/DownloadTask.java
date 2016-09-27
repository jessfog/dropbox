package com.jessfog.dropbox.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestUtil;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.sharing.CreateSharedLinkWithSettingsErrorException;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.jessfog.dropbox.MainActivity;
import com.jessfog.dropbox.model.Photo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jessemartinez on 9/23/16.
 */

public class DownloadTask extends AsyncTask {

    private DbxClientV2 mDbxClient;
    private String mPath;
    private Context mContext;
    private List<Photo> mPhotoList = new ArrayList<>();
    private DownloadTaskDelegate mDelegate;

    public DownloadTask(DbxClientV2 mDbxClient, String path, Context context) {
        this.mDbxClient = mDbxClient;
        this.mPath = path;
        this.mContext = context;
    }

    @Override
    protected Object doInBackground(Object[] params) {

        List<Metadata> files;
        try {
            files = mDbxClient.files().listFolder("").getEntries();
            if(files != null) {
                for(Metadata meta : files) {
                    try {
                        Log.i("DROPBOX1", "Encoded url" + DbxRequestUtil.encodeUrlParam(meta.getPathLower()));
                        SharedLinkMetadata sharedLinkMetadata = mDbxClient.sharing().createSharedLinkWithSettings(meta.getPathDisplay());
                        Photo p = new Photo(meta);
                        p.setUrl(sharedLinkMetadata);
                        mPhotoList.add(p);
                        Log.i("DROPBOX", "Share url: " + sharedLinkMetadata.getUrl());
                    } catch (CreateSharedLinkWithSettingsErrorException ex) {
                        System.out.println(ex);
                    } catch (DbxException ex) {
                        System.out.println(ex);
                    }
                }
            }
        } catch (DbxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        Toast.makeText(mContext, "Folder contents received", Toast.LENGTH_SHORT).show();
        if(mPhotoList != null && mPhotoList.size() > 0 ) {
            mDelegate.onTaskComplete(mPhotoList);
        }
    }

    public void setDelegate(MainActivity mainActivity) {
        mDelegate = mainActivity;
    }

    public interface DownloadTaskDelegate {

        void onTaskComplete(List<Photo> list);
    }
}
