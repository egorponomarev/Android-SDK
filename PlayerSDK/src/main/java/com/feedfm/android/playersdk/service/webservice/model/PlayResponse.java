package com.feedfm.android.playersdk.service.webservice.model;

import com.feedfm.android.playersdk.model.Play;
import com.google.gson.annotations.SerializedName;

/**
 * Created by mharkins on 8/21/14.
 */
public class PlayResponse extends FeedFMResponse {
    @SerializedName("play") private Play play;

    public Play getPlay() {
        return play;
    }
}
