package com.jessfog.dropbox;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import com.bumptech.glide.Glide;


public class DetailActivity extends AppCompatActivity {

    ImageView mDetailPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle p = getIntent().getExtras();
        setupUI(p.getString("photo_url"));
    }

    private void setupUI(String url) {
        mDetailPhoto = (ImageView) findViewById(R.id.detail_image);
        if(url != null) {
            Glide.with(this)
                    .load(url).thumbnail(0.5f)
                    .crossFade()
                    .into(mDetailPhoto);
        }
    }

}
