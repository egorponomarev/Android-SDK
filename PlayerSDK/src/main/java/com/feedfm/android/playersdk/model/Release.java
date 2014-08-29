package com.feedfm.android.playersdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mharkins on 8/21/14.
 */
public class Release {
    @SerializedName("id")
    private Integer id;
    @SerializedName("title")
    private String title;

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
