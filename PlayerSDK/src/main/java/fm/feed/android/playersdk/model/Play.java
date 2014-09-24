package fm.feed.android.playersdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * The Play information pertaining to a track/song
 * <p/>
 * Created by mharkins on 8/21/14.
 */
public class Play {
    @SerializedName("id")
    private String id;
    @SerializedName("station")
    private Station station;
    @SerializedName("audio_file")
    private AudioFile audioFile;

    public static enum LikeState {
        NONE(null),
        LIKED("liked"),
        DISLIKED("disliked");

        private String value;

        LikeState(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private LikeState likeState = LikeState.NONE;

    /**
     * Id of the Play
     *
     * @return The Id of the Play
     */
    public String getId() {
        return id;
    }

    /**
     * Station this {@link fm.feed.android.playersdk.model.Play} belongs too
     *
     * @return The station this {@link fm.feed.android.playersdk.model.Play} belongs too
     */
    public Station getStation() {
        return station;
    }

    /**
     * {@link fm.feed.android.playersdk.model.AudioFile} object containing information pertaining to the track
     *
     * @return The {@link fm.feed.android.playersdk.model.AudioFile} object.
     */
    public AudioFile getAudioFile() {
        return audioFile;
    }

    public LikeState getLikeState() {
        return likeState;
    }

    public void setLikeState(LikeState likeState) {
        this.likeState = likeState;
    }
}
