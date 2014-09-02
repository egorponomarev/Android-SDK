package com.feedfm.android.playersdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mharkins on 8/21/14.
 */
public class Placement {
    @SerializedName("id")
    private Integer id;
    @SerializedName("name")
    private String name;

    public Placement(int id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
