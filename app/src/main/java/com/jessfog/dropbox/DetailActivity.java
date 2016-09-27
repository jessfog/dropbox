package com.jessfog.dropbox;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;


public class DetailActivity extends AppCompatActivity {

    ImageView mDetailPhoto;
    private ProgressDialog pd;
    private TextView mPhotoTitle;
    private TextView mPhotoDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle b = getIntent().getExtras();
        setupUI(b);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        if(pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

    private void setupUI(Bundle b) {
        mDetailPhoto = (ImageView) findViewById(R.id.detail_image);
        mPhotoTitle = (TextView) findViewById(R.id.detail_image_title);
        mPhotoDetails = (TextView) findViewById(R.id.detail_description);
        mPhotoDetails.setVisibility(View.GONE);
        String url = b.getString("photo_url");
        String title = b.getString("photo_title");
        if(mDetailPhoto != null && url != null) {
            pd = ProgressDialog.show(this, "", "Downloading from Dropbox, Please Wait", false);
        }
        if(mPhotoTitle != null && title != null) {
            mPhotoTitle.setText(title);
        }
        if(url != null) {
            Glide.with(this)
                    .load(url).thumbnail(0.5f)
                    .crossFade().listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            pd.dismiss();
                            if(mPhotoDetails.getVisibility() != View.VISIBLE) {
                                mPhotoDetails.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mPhotoDetails.setVisibility(View.VISIBLE);
                                        mPhotoDetails.setAlpha(0.0f);
                                        mPhotoDetails.setY(mDetailPhoto.getY());
                                        mPhotoDetails.animate().translationY(0).alpha(1.0f);
                                    }
                                }, 1000);
                            }
                            return false;
                        }
            })
                    .into(mDetailPhoto);
        }
    }

}
