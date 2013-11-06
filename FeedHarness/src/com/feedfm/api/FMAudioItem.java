package com.feedfm.api;

import org.json.JSONObject;

import java.net.URL;

/**
 * Created by Thomas on 9/16/13.
 */
public class FMAudioItem {


    public final static String ID = "id";
    public final static String AUDIO_FILE = "audio_file";
    public final static String URL = "URL";
    public final static String TRACK = "track";
    public final static String TITLE = "title";
    public final static String ARTIST = "artist";
    public final static String NAME = "name";
    public final static String RELEASE = "relesase";
    public final static String CODEC = "codec";
    public final static String DURATION_IN_SECONDS = "duration";
    public final static String BITRATE = "bitrate";




    private String playid;
    private String name;
    private String artist;
    private String album;
    private Double duration;
    private URL contentURL;
    private String codec;
    private double bitrate;




    public FMAudioItem(JSONObject jsonDictionary) {

        if(jsonDictionary != null) {



        }
    }

    public String getPlayid() {
        return playid;
    }

    public void setPlayid(String playid) {
        this.playid = playid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public URL getContentURL() {
        return contentURL;
    }

    public void setContentURL(URL contentURL) {
        this.contentURL = contentURL;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public double getBitrate() {
        return bitrate;
    }

    public void setBitrate(double bitrate) {
        this.bitrate = bitrate;
    }
}
