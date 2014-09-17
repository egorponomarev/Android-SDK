package fm.feed.android.playersdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Audio file information
 * <p/>
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

    /**
     * Duration of the track in seconds
     *
     * @return The duration of the track in seconds
     */
    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    /**
     * Codec used to encode the Audio file
     *
     * @return The encoding used: aac or mp3
     */
    public String getCodec() {
        return codec;
    }

    /**
     * Bitrate of the Audio file
     *
     * @return an integer and a multiple of 1,000. A value of "96" would imply a maximum bitrate of 96kb/s.
     */
    public String getBitrate() {
        return bitrate;
    }

    /**
     * Url of the Audio stream
     *
     * @return The url of the Audio stream
     */
    public String getUrl() {
        return url;
    }

    /**
     * Information about the Track
     * <p>ie. Title of this song.</p>
     *
     * @return the {@link fm.feed.android.playersdk.model.Track} information.
     */
    public Track getTrack() {
        return track;
    }

    /**
     * Information about the Album
     *
     * @return the {@link fm.feed.android.playersdk.model.Release} (Album) information.
     */
    public Release getRelease() {
        return release;
    }

    /**
     * Information about the Artist
     *
     * @return the {@link fm.feed.android.playersdk.model.Artist} information.
     */
    public Artist getArtist() {
        return artist;
    }
}
