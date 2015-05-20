package fm.feed.android.playersdk.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Placement information
 * <p>
 * The placements configurable on your Feed.FM dashboard.
 * </p>
 * <p>
 * The {@code placement ID} is viewable through your Feed.FM account:
 * <ol>
 * <li>Select an App in <b><a href="http://developer.feed.fm/dashboard">Your Apps and Websites</a></b></li>
 * <li>Go to tab <b>Developer Codes and IDs</b></li>
 * </ol>
 * </p>
 * <p/>
 * Created by mharkins on 8/21/14.
 */
public class Placement {
    @SerializedName("id")
    private Integer id;
    @SerializedName("name")
    private String name;

    private transient List<Station> mStationList;

    public Placement(int id) {
        this.id = id;
    }

    /**
     * Id of the Placement
     * <p/>
     * <p>
     * The {@code placement ID} is viewable through your Feed.FM account:
     * <ol>
     * <li>Select an App in <b><a href="http://developer.feed.fm/dashboard">Your Apps and Websites</a></b></li>
     * <li>Go to tab <b>Developer Codes and IDs</b></li>
     * </ol>
     * </p>
     *
     * @return The id of the placement.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Name of the placement
     * <p/>
     * <p>The name of your App on the Feed.FM dashboard.</p>
     * <p/>
     * <p>
     * To change the name of the placement:
     * <ol>
     * <li>Select an App in <b><a href="http://developer.feed.fm/dashboard">Your Apps and Websites</a></b></li>
     * <li>Go to tab <b>Config & Settings</b></li>
     * <li>Type in a new <b>Name</b> for your placement</li>
     * <li>Click <b>Save Changes</b></li>
     * </ol>
     * </p>
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * List of {@link Station}s for this placement
     *
     * @return The list of {@link Station}s for this placement
     */
    public List<Station> getStationList() {
        return mStationList;
    }

    public void setStationList(List<Station> mStationList) {
        this.mStationList = mStationList;
    }
}
