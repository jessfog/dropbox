package com.jessfog.dropbox.tasks;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

/**
 * Created by jessemartinez on 9/21/16.
 */

public class UserAccountTask extends AsyncTask<Void, Void, FullAccount> {

    private DbxClientV2 mDbxClient;
    private TaskDelegate  mDelegate;
    private Exception mException;

    public UserAccountTask(DbxClientV2 mDbxClient, TaskDelegate mDelegate){
        this.mDbxClient =mDbxClient;
        this.mDelegate = mDelegate;
    }

    /*
        get the users Dropbox FullAccount
     */
    @Override
    protected FullAccount doInBackground(Void... params) {
        try {
            return mDbxClient.users().getCurrentAccount();
        } catch (DbxException e) {
            e.printStackTrace();
            mException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(FullAccount account) {
        super.onPostExecute(account);

        if (account != null && mException == null){
            mDelegate.onAccountReceived(account);
        }
        else {
            mDelegate.onError(mException);
        }
    }

    public interface TaskDelegate {
        void onAccountReceived(FullAccount account);
        void onError(Exception mException);
    }
}