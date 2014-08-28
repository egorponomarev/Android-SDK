package com.feedfm.android.playersdk.mocks;

import com.feedfm.android.playersdk.Player;
import com.feedfm.android.playersdk.model.Placement;
import com.feedfm.android.playersdk.model.Station;

import java.util.List;

/**
 * Created by mharkins on 8/27/14.
 */
public class DummyPlayerPlayerListener implements Player.PlayerListener, Player.NavListener, Player.SocialListener {
    public boolean didCallPlayerInitialized = false;
    public boolean didCallPlacementChanged = false;
    public boolean didCallStationChanged = false;
    public boolean didCallTrackChanged = false;
    public boolean didCallPlaybackStateChanged = false;
    public boolean didCallSkipFailed = false;
    public boolean didCallNotInUS = false;
    public boolean didCallLiked = false;
    public boolean didCallUnliked = false;
    public boolean didCallDisliked = false;


    @Override
    public void onPlayerInitialized() {
        didCallPlayerInitialized = true;
    }

    @Override
    public void onPlacementChanged(Placement placement, List<Station> stationList) {
        didCallPlacementChanged = true;
    }

    @Override
    public void onStationChanged(Station station) {
        didCallStationChanged = true;
    }

    @Override
    public void onTrackChanged(Placement placement, List<Station> stationList) {
        didCallTrackChanged = true;
    }

    @Override
    public void onPlaybackStateChanged(Placement placement, List<Station> stationList) {
        didCallPlaybackStateChanged = true;
    }

    @Override
    public void onSkipFailed() {
        didCallSkipFailed = true;
    }

    @Override
    public void onNotInUS() {
        didCallNotInUS = true;
    }

    @Override
    public void onLiked() {
        didCallLiked = true;
    }

    @Override
    public void onUnliked() {
        didCallUnliked = true;
    }

    @Override
    public void onDisliked() {
        didCallDisliked = true;
    }
}