package com.feedfm.android.playersdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mharkins on 8/21/14.
 */
public class Client {
    @SerializedName("success") private boolean success;
    @SerializedName("client_id") private String clientId;

    public boolean isSuccess() {
        return success;
    }

    public String getClientId() {
        return clientId;
    }
}
