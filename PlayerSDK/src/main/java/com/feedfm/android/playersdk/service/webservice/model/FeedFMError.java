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

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("{code: %d, message: \"%s\", status: %s}", getCode(), getMessage(), getStatus());
    }
}
