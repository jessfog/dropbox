package com.jessfog.dropbox;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.v2.users.FullAccount;
import com.dropbox.core.v2.DbxClientV2;
import com.jessfog.dropbox.adapter.PhotoAdapter;
import com.jessfog.dropbox.model.Photo;
import com.jessfog.dropbox.tasks.DownloadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements DownloadTask.DownloadTaskDelegate,
                        UploadTask.UploadTaskDelegate{

    private static final int IMAGE_REQUEST_CODE = 101;
    private String ACCESS_TOKEN;
    private DbxClientV2 mDBxClient;
    private View coordinatorLayout;
    private TextView mInstructions;

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
                    UploadTask up =  new UploadTask(DropboxClient.getClient(ACCESS_TOKEN), file, getApplicationContext(), coordinatorLayout);
                    up.setDelegate(this);
                    up.execute();
                }
            }
        }
    }

    protected void getFolderContents() {
        mDBxClient = DropboxClient.getClient(ACCESS_TOKEN);
        DownloadTask downTask = new DownloadTask(mDBxClient, "/", this);
        downTask.setDelegate(this);
        downTask.execute();
    }

    protected void getUserAccount() {
        if (ACCESS_TOKEN == null)return;
        new UserAccountTask(DropboxClient.getClient(ACCESS_TOKEN), new UserAccountTask.TaskDelegate() {
            @Override
            public void onAccountReceived(FullAccount account) {
                //Print account's info
                Log.d("User", account.getEmail());
                Log.d("User", account.getName().getDisplayName());
                Log.d("User", account.getAccountType().name());
                updateUI(account);
            }
            @Override
            public void onError(Exception error) {
                Log.d("User", "Error receiving account details.");
            }
        }).execute();
    }

    private void updateUI(FullAccount account) {
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        ImageView profile = (ImageView) findViewById(R.id.imageView);
        TextView instructionsTV = (TextView) findViewById(R.id.instructions);
        /* TextView email = (TextView) findViewById(R.id.email_textView);

        name.setText(account.getName().getDisplayName());
        email.setText(account.getEmail());*/
        Picasso.with(this)
                .load(account.getProfilePhotoUrl())
                .resize(200, 200)
                .into(profile);
    }

    private void upload() {
        if (ACCESS_TOKEN == null)return;
        //Select image to upload
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent,
                "Upload to Dropbox"), IMAGE_REQUEST_CODE);
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


    private void initViews(List<Photo> list){
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.card_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        PhotoAdapter adapter = new PhotoAdapter(this,(ArrayList)list);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onTaskComplete(List<Photo> list) {
        initViews(list);
    }

    @Override
    public void onUploadTaskComplete() {
        Toast.makeText(this, getResources().getString(R.string.upload_success), Toast.LENGTH_SHORT).show();
        if(mInstructions != null) {
            mInstructions.setVisibility(View.GONE);
        }
    }
}
