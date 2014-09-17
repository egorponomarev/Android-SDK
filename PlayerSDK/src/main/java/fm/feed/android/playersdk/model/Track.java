package fm.feed.android.playersdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Track information
 * <p/>
 * Created by mharkins on 8/21/14.
 */
public class Track {
    @SerializedName("id")
    private Integer id;
    @SerializedName("title")
    private String title;

    /**
     * Title of the Track/Song
     *
     * @return The title of the Track.
     */
    public String getTitle() {
        return title;
    }
}
