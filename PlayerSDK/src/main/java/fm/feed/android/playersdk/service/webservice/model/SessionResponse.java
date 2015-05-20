package fm.feed.android.playersdk.service.webservice.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.model.Session;
import fm.feed.android.playersdk.model.Station;


public class SessionResponse extends FeedFMResponse {
    @SerializedName("session")
    private Session session;
    @SerializedName("placement")
    private Placement placement;
    @SerializedName("stations")
    private List<Station> stations;


    public Placement getPlacement() {
        return placement;
    }

    public Session getSession() {
        return session;
    }

    public List<Station> getStations() {
        return stations;
    }

    @Override
    public Session getModel() {
        Session session = getSession();

        // if there is a placement, throw that in the session along with the
        // stations it has.
        Placement placement = getPlacement();

        if (placement != null) {
            placement.setStationList(getStations());

            // stuff placement into session
            session.setPlacement(placement);
        }

        return session;
    }
}
