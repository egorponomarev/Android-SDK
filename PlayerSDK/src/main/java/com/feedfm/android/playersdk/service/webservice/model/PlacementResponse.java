package com.feedfm.android.playersdk.service.webservice.model;

import com.feedfm.android.playersdk.model.Placement;
import com.feedfm.android.playersdk.model.Station;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by mharkins on 8/21/14.
 */
public class PlacementResponse extends FeedFMResponse {
    @SerializedName("placement") private Placement     placement;
    @SerializedName("stations") private  List<Station> stations;

    public Placement getPlacement() {
        return placement;
    }

    public List<Station> getStations() {
        return stations;
    }
}
