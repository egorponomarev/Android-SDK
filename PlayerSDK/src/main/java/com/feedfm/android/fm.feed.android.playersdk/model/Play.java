package fm.feed.android.playersdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mharkins on 8/21/14.
 */
public class Play {
    @SerializedName("id")
    private String id;
    @SerializedName("station")
    private Station station;
    @SerializedName("audio_file")
    private AudioFile audioFile;

    public String getId() {
        return id;
    }

    public Station getStation() {
        return station;
    }

    public AudioFile getAudioFile() {
        return audioFile;
    }
}
