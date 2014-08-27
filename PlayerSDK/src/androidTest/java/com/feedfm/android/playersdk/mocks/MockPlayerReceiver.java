package com.feedfm.android.playersdk.mocks;

import com.feedfm.android.playersdk.model.Placement;
import com.feedfm.android.playersdk.model.Station;
import com.feedfm.android.playersdk.service.bus.Credentials;
import com.feedfm.android.playersdk.service.bus.PlayerAction;
import com.squareup.otto.Subscribe;

/**
 * Created by mharkins on 8/27/14.
 */
public class MockPlayerReceiver {

    public boolean postedCredentials = false;
    public boolean postedPlacement = false;
    public boolean postedStation = false;
    public boolean postedTune = false;
    public boolean postedPlay = false;
    public boolean postedPause = false;
    public boolean postedSkip = false;
    public boolean postedLike = false;
    public boolean postedDislike = false;
    public boolean postedUnlike = false;

    @Subscribe
    public void handlePostCredentials(Credentials creds) {
        postedCredentials = creds != null;
    }

    @Subscribe
    public void handlePostPlacement(Placement placement) {
        postedPlacement = placement != null;
    }

    @Subscribe
    public void handlePostStationId(Station station) {
        postedStation = station != null;
    }

    @Subscribe
    public void handlePostAction(PlayerAction action) {
        switch (action.getAction()) {
            case TUNE:
                postedTune = true;
                break;
            case PLAY:
                postedPlay = true;
                break;
            case SKIP:
                postedSkip = true;
                break;
            case PAUSE:
                postedPause = true;
                break;
            case LIKE:
                postedLike = true;
                break;
            case UNLIKE:
                postedUnlike = true;
                break;
            case DISLIKE:
                postedDislike = true;
                break;
        }
    }

}
