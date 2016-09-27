package com.jessfog.dropbox.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
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

    public DownloadTask(DbxClientV2 dbxClient, String path, Context context) {
        mDbxClient = dbxClient;
        mPath = path;
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object[] params) {

        List<Metadata> files;
        try {
            files = mDbxClient.files().listFolder(mPath).getEntries();
            if(files != null) {
                for(Metadata meta : files) {
                    try {
                        SharedLinkMetadata sharedLinkMetadata = mDbxClient.sharing().createSharedLinkWithSettings(meta.getPathDisplay());
                        Photo p = new Photo(meta);
                        p.setUrl(sharedLinkMetadata);
                        mPhotoList.add(p);
                    } catch (CreateSharedLinkWithSettingsErrorException ex) {
                        System.out.println(ex.getMessage());
                    } catch (DbxException ex) {
                        System.out.println(ex.getMessage());
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
        if(mPhotoList != null && mPhotoList.size() > 0 ) {
            mDelegate.onTaskComplete(mPhotoList);
        }
    }

    public void setDelegate(MainActivity mainActivity) {
        mDelegate = mainActivity;
    }

    /*
        Interface to notify calling activity that task  has completed
     */
    public interface DownloadTaskDelegate {
        void onTaskComplete(List<Photo> list);
    }
}
