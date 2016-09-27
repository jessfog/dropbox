package com.jessfog.dropbox.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by jessemartinez on 9/23/16.
 */

import com.bumptech.glide.Glide;
import com.jessfog.dropbox.DetailActivity;
import com.jessfog.dropbox.MainActivity;
import com.jessfog.dropbox.R;
import com.jessfog.dropbox.model.Photo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
    private ArrayList<Photo> mPhotos;
    private Context context;

    public PhotoAdapter(Context context,ArrayList<Photo> photos) {
        this.context = context;
        this.mPhotos = photos;

    }

    @Override
    public PhotoAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.photo_row_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {

        viewHolder.nameTV.setText(mPhotos.get(i).getName());
//        Picasso.with(context).load(mPhotos.get(i).getUrl()).into(viewHolder.photoIV);
        final String url = mPhotos.get(i).getUrl();
        Glide.with(context)
                .load(url).thumbnail(0.5f)
                .crossFade()
                .into(viewHolder.photoIV);
        viewHolder.photoIV.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                Bundle b= new Bundle();
                b.putString("photo_url", url);
                Intent i = new Intent(context, DetailActivity.class);
                i.putExtras(b);
                View sharedView = viewHolder.photoIV;
                String transitionName = context.getString(R.string.photo_transition);
                ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity)context, sharedView, transitionName);
                context.startActivity(i, transitionActivityOptions.toBundle());
            }
        });

    }

    @Override
    public int getItemCount() {
        return mPhotos.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView nameTV;
        ImageView photoIV;
        public ViewHolder(View view) {
            super(view);

            nameTV = (TextView)view.findViewById(R.id.name_tv);
            photoIV = (ImageView)view.findViewById(R.id.photo_iv);
        }
    }
}