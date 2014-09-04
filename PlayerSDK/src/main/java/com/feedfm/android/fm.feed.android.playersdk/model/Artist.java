package fm.feed.android.playersdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mharkins on 8/21/14.
 */
public class Artist {
    @SerializedName("id")
    private Integer id;
    @SerializedName("name")
    private String name;

    // TODO: remove constructor.
    public Artist(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
