package com.jessfog.dropbox.model;

import android.util.Log;

import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

/**
 * Created by jessemartinez on 9/23/16.
 */

public class Photo {
    private String mName;
    private String mUrl;

    public Photo(String name, String url) {
        mName = name;
        mUrl = url;
    }

    public Photo(Metadata meta) {
        mName = meta.getName();
    }

    public String getName() {
        return mName;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(SharedLinkMetadata share) {
        mUrl = share.getUrl();
        mUrl = mUrl.replace("https://www.dropbox.com", "https://dl.dropboxusercontent.com");
        Log.i("DROPBOX", "Photo url: " + mUrl);
//        mUrl = "http://www.miniusa.com/content/dam/mini/shopping-tools/learning/build-price-a-mini/build-config-f56-cooper-s.png/jcr:content/renditions/cq5dam.web.500.1000.png";
    }
}
