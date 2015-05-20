package fm.feed.android.playersdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * A session holds the unique client id and a boolean that
 * determines whether the client will be able to play music or not.
 */
public class Session {

    @SerializedName("available")
    private boolean available;

    @SerializedName("client_id")
    private String clientId;

    private transient Placement placement;

    public boolean isAvailable() {
        return available;
    }

    public String getClientId() {
        return clientId;
    }

    public void setPlacement(Placement placement) {
        this.placement = placement;
    }

    public Placement getPlacement() {
        return placement;
    }
}
