package com.feedfm.android.playersdk.service.webservice.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mharkins on 8/21/14.
 */
public class FeedFMResponse {
    @SerializedName("success")
    private boolean success;
    @SerializedName("error")
    private FeedFMError error;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public FeedFMError getError() {
        return error;
    }
}
