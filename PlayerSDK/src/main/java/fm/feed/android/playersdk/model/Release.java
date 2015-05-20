package fm.feed.android.playersdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Album Information
 * <p/>
 * Created by mharkins on 8/21/14.
 */
public class Release {
       @SerializedName("id")
    private Integer id;
    @SerializedName("title")
    private String title;

    /**
     * Album Title
     *
     * @return The Album Title
     */
    public String getTitle() {
        return title;
    }
}
