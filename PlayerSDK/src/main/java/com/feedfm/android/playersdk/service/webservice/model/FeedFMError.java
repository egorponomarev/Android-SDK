package com.feedfm.android.playersdk.service.webservice.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mharkins on 8/21/14.
 */
public class FeedFMError {
    @SerializedName("code") private    int    code;
    @SerializedName("message") private String message;
    @SerializedName("status") private  int    status;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return String.format("{code: %d, message: \"%s\", status: %s}", getCode(), getMessage(), getStatus());
    }
}
