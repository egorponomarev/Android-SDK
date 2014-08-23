package com.feedfm.android.playersdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mharkins on 8/21/14.
 */
public class Station {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;

    public Station(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
