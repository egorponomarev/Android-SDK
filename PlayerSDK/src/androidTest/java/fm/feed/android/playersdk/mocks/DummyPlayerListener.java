package fm.feed.android.playersdk.mocks;

import java.util.List;

import fm.feed.android.playersdk.NavListener;
import fm.feed.android.playersdk.NotificationBuilder;
import fm.feed.android.playersdk.PlayerError;
import fm.feed.android.playersdk.PlayerListener;
import fm.feed.android.playersdk.SocialListener;
import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.PlayInfo;

/**
 * Created by mharkins on 8/27/14.
 */
public class DummyPlayerListener implements PlayerListener, NavListener, SocialListener {
    public boolean didCallPlayerInitialized = false;
    public boolean didCallPlacementChanged = false;
    public boolean didCallStationChanged = false;
    public boolean didCallTrackChanged = false;
    public boolean didCallEndOfPlaylist = false;
    public boolean didCallShowNotification = false;
    public boolean didCallPlaybackStateChanged = false;
    public boolean didCallSkipFailed = false;
    public boolean didCallNotInUS = false;
    public boolean didCallOnError = false;
    public boolean didCallLiked = false;
    public boolean didCallUnliked = false;
    public boolean didCallDisliked = false;

    public boolean didCallBufferUpdate = false;
    public boolean didCallProgressUpdate = false;
    public int bufferUpdate = 0;
    public int progressUpdate = 0;

    public PlayInfo.State state = PlayInfo.State.WAITING;



    @Override
    public void onPlacementChanged(Placement placement, List<Station> stationList) {
        didCallPlacementChanged = true;
    }

    @Override
    public void onStationChanged(Station station) {
        didCallStationChanged = true;
    }

    @Override
    public void onTrackChanged(Play play) {
        didCallTrackChanged = true;
    }

    @Override
    public void onSkipFailed() {
        didCallSkipFailed = true;
    }

    @Override
    public void onEndOfPlaylist() {
        didCallEndOfPlaylist = true;
    }

    @Override
    public void onPlayerInitialized(PlayInfo playInfo) {
        didCallPlayerInitialized = true;
    }

    @Override
    public NotificationBuilder getNotificationBuilder() {
        return null;
    }

    @Override
    public void onSkipStatusChange(boolean skippable) {

    }

    @Override
    public void onPlaybackStateChanged(PlayInfo.State state) {
        didCallPlaybackStateChanged = true;
        this.state = state;
    }

    @Override
    public void onError(PlayerError playerError) {
        didCallOnError = true;
    }

    @Override
    public void onBufferUpdate(Play play, int percentage) {
        didCallBufferUpdate = true;
        bufferUpdate = percentage;
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

    @Override
    public void onProgressUpdate(Play play, int elapsedTime, int totalTime) {
        didCallProgressUpdate = true;
        progressUpdate = elapsedTime;
    }
}
