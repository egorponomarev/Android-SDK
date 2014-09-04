package fm.feed.android.playersdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mharkins on 8/21/14.
 */
public class Station {
    @SerializedName("id")
    private Integer id;
    @SerializedName("name")
    private String name;

    public Station(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
