package fm.feed.android.playersdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Artist information
 * <p/>
 * Created by mharkins on 8/21/14.
 */
public class Artist {
    @SerializedName("id")
    private Integer id;
    @SerializedName("name")
    private String name;

    /**
     * Artist name
     *
     * @return The Artist's name.
     */
    public String getName() {
        return name;
    }
}
