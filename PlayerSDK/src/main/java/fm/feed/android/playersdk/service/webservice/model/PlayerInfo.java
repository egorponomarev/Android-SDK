package fm.feed.android.playersdk.service.webservice.model;

import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.model.Station;

import java.util.List;

/**
 * Created by mharkins on 8/29/14.
 */
public class PlayerInfo {
    protected String mClientId;

    private List<Station> mStationList;

    private Placement mPlacement = null;
    private Station mStation = null;

    private boolean mSkippable;

    public String getClientId() {
        return mClientId;
    }

    public void setClientId(String mClientId) {
        this.mClientId = mClientId;
    }

    public List<Station> getStationList() {
        return mStationList;
    }

    public void setStationList(List<Station> mStationList) {
        this.mStationList = mStationList;
    }

    public boolean hasStationList() {
        return this.mStationList != null && this.mStationList.size() > 0;
    }

    public Placement getPlacement() {
        return mPlacement;
    }

    public void setPlacement(Placement mPlacement) {
        this.mPlacement = mPlacement;
    }

    public Station getStation() {
        return mStation;
    }

    public void setStation(Station mStation) {
        this.mStation = mStation;
    }

    public boolean isSkippable() {
        return mSkippable;
    }

    public void setSkippable(boolean skippable) {
        this.mSkippable = skippable;
    }
}
