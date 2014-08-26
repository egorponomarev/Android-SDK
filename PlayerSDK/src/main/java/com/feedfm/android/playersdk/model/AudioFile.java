package com.feedfm.android.playersdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mharkins on 8/21/14.
 */
public class AudioFile {
    @SerializedName("id")
    private String id;

    @SerializedName("duration_in_seconds")
    private String durationInSeconds;
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

    public String getId() {
        return id;
    }

    public String getDurationInSeconds() {
        return durationInSeconds;
    }

    public String getCodec() {
        return codec;
    }

    public String getBitrate() {
        return bitrate;
    }

    public String getUrl() {
        return url;
    }

    public Track getTrack() {
        return track;
    }

    public Release getRelease() {
        return release;
    }

    public Artist getArtist() {
        return artist;
    }
}
