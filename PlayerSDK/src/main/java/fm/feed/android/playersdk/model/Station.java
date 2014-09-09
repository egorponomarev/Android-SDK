package fm.feed.android.playersdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Station information
 * <p/>
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

    /**
     * Id of this {@link fm.feed.android.playersdk.model.Station}
     *
     * @return The Id of this {@link fm.feed.android.playersdk.model.Station}.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Name of the station
     *
     * @return The name of the station.
     */
    public String getName() {
        return name;
    }
}
