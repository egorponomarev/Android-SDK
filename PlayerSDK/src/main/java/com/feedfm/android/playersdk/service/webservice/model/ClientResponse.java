package com.feedfm.android.playersdk.service.webservice.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mharkins on 8/21/14.
 */
public class ClientResponse extends FeedFMResponse{
    @SerializedName("client_id") private String  clientId;

    public String getClientId() {
        return clientId;
    }
}
