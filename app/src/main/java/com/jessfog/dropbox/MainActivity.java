package com.jessfog.dropbox;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.core.v2.users.FullAccount;
import com.dropbox.core.v2.DbxClientV2;
import com.jessfog.dropbox.adapter.PhotoAdapter;
import com.jessfog.dropbox.api.DropboxClient;
import com.jessfog.dropbox.api.URI_to_Path;
import com.jessfog.dropbox.model.Photo;
import com.jessfog.dropbox.tasks.DownloadTask;
import com.jessfog.dropbox.tasks.UploadTask;
import com.jessfog.dropbox.tasks.UserAccountTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements DownloadTask.DownloadTaskDelegate,
                        UploadTask.UploadTaskDelegate{

    private static final int IMAGE_REQUEST_CODE = 101;
    private String ACCESS_TOKEN;
    private String TAG = "DROPBOX";
    private DbxClientV2 mDBxClient;
    private View mCoordinatorLayout;
    private TextView mInstructions;
    private TextView mInstructionsTV;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!tokenExists()) {
            //No token
            //Back to LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        ACCESS_TOKEN = retrieveAccessToken();
        getUserAccount();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload();
            }
        });

        getFolderContents();
        mCoordinatorLayout = findViewById(R.id.coordinatorLayout);
        Snackbar snackbar = Snackbar
                .make(mCoordinatorLayout, getResources().getString(R.string.downloading_photos), Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;
        // Check which request we're responding to
        if (requestCode == IMAGE_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                //Image URI received
                File file = new File(URI_to_Path.getPath(getApplication(), data.getData()));
                if (file != null) {
                    //Initialize UploadTask
                    UploadTask up =  new UploadTask(DropboxClient.getClient(ACCESS_TOKEN), file, this, mCoordinatorLayout);
                    up.setDelegate(this);
                    up.execute();
                }
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    /*
        Updates UI widgets
     */
    private void updateUI(FullAccount account) {
        ImageView profile = (ImageView) findViewById(R.id.imageView);
        mInstructionsTV = (TextView) findViewById(R.id.instructions);
        TextView nameTV = (TextView) findViewById(R.id.profileName);
        TextView emailTV = (TextView) findViewById(R.id.profileEmail);

        nameTV.setText(account.getName().getDisplayName());
        emailTV.setText(account.getEmail());
        Picasso.with(this)
                .load(account.getProfilePhotoUrl())
                .resize(200, 200)
                .into(profile);
    }

    /*
        Retrieves Dropbox user from Token
     */
    protected void getUserAccount() {
        if (ACCESS_TOKEN == null)return;
        new UserAccountTask(DropboxClient.getClient(ACCESS_TOKEN), new UserAccountTask.TaskDelegate() {
            @Override
            public void onAccountReceived(FullAccount account) {
                //Print account's info
                Log.d(TAG, account.getEmail());
                Log.d(TAG, account.getName().getDisplayName());
                Log.d(TAG, account.getAccountType().name());
                updateUI(account);
            }
            @Override
            public void onError(Exception error) {
                Log.d("User", "Error receiving account details.");
            }
        }).execute();
    }

    private boolean tokenExists() {
        SharedPreferences prefs = getSharedPreferences("com.jessfog.dropbox", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access-token", null);
        return accessToken != null;
    }

    private String retrieveAccessToken() {
        //check if ACCESS_TOKEN is stored on previous app launches
        SharedPreferences prefs = getSharedPreferences("com.jessfog.dropbox", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access-token", null);
        if (accessToken == null) {
            Log.d("AccessToken Status", "No token found");
            return null;
        } else {
            //accessToken already exists
            Log.d("AccessToken Status", "Token exists");
            return accessToken;
        }
    }

    /*
        Makes async task call to retrieve folder contents of this app in Dropbox
     */
    protected void getFolderContents() {
        mDBxClient = DropboxClient.getClient(ACCESS_TOKEN);
        DownloadTask downTask = new DownloadTask(mDBxClient, "", this);
        downTask.setDelegate(this);
        downTask.execute();
    }

    /*
        Opens image picker to select a photo to be uploaded
     */
    private void upload() {
        if (ACCESS_TOKEN == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if (isStoragePermissionGranted()) {
                //listener will handle
            }
        } else {

        }
    }

    private void displayImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent,
                "Upload to Dropbox"), IMAGE_REQUEST_CODE);
    }

    /*
        Sets up photo scrolling list from list of photos received.
     */
    private void setupPhotoList(List<Photo> list, boolean scroll){
        mRecyclerView = (RecyclerView)findViewById(R.id.card_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(layoutManager);

        PhotoAdapter adapter = new PhotoAdapter(this,(ArrayList)list);
        mRecyclerView.setAdapter(adapter);
        if(scroll) {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    mRecyclerView.smoothScrollToPosition(mRecyclerView.getChildCount());
                }
            });
        }
    }

    /*
        Implement interface method to update UI with photos retreived.
     */
    @Override
    public void onTaskComplete(List<Photo> list) {
        if(mInstructions != null) {
            mInstructions.setVisibility(View.GONE);
        }
        setupPhotoList(list, false);
        mInstructionsTV.setVisibility(View.GONE);
    }

    /*
        Implement interface from Upload Task
     */
    @Override
    public void onUploadTaskComplete() {
        getFolderContents();
    }

    /*
        Check to see if storage permission has been granted
     */
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission. READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                displayImageChooser();
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission. READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    /*
        Callback of permission request - user's response yes/no
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    displayImageChooser();

                } else {
                    // permission denied

                }
                return;
            }

        }
    }
}
