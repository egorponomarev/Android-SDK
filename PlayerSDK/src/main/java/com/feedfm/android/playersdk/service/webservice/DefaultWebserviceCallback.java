package com.feedfm.android.playersdk.service.webservice;

import android.util.Log;

import com.feedfm.android.playersdk.service.webservice.model.FeedFMError;

/**
 * Created by mharkins on 8/29/14.
 */
public abstract class DefaultWebserviceCallback<T> implements Webservice.Callback<T> {
    private static final String TAG = "";

    @Override
    public void onSuccess(T t) {

    }

    @Override
    public void onFailure(FeedFMError error) {
        if (error != null) {
            Log.e(TAG, String.format("Skip failed: %s", error.toString()));
        }
    }
}
