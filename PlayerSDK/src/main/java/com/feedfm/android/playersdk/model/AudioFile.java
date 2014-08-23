package com.feedfm.android.playersdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mharkins on 8/21/14.
 */
public class AudioFile {
    @SerializedName("id")
    private String id;

    @SerializedName("duration_in_seconds")
    private int durationInSeconds;
    @SerializedName("codec")
    private String codec;
    @SerializedName("bitrate")
    private String bitrate;
    @SerializedName("url")
    private String url;

    @SerializedName("track")
    private Track track;

    @SerializedName("release")
    private Release release;

    @SerializedName("artist")
    private Artist artist;

}
