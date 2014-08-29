package com.feedfm.android.playersdk;

import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import com.feedfm.android.playersdk.model.Placement;
import com.feedfm.android.playersdk.model.Play;
import com.feedfm.android.playersdk.model.Station;
import com.feedfm.android.playersdk.service.bus.BufferUpdate;
import com.feedfm.android.playersdk.service.bus.Credentials;
import com.feedfm.android.playersdk.service.bus.EventMessage;
import com.feedfm.android.playersdk.service.bus.OutStationWrap;
import com.feedfm.android.playersdk.service.bus.PlayerAction;
import com.feedfm.android.playersdk.model.PlayerLibraryInfo;
import com.feedfm.android.playersdk.service.bus.ProgressUpdate;
import com.feedfm.android.playersdk.service.bus.SingleEventBus;
import com.feedfm.android.playersdk.service.PlayerService;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

/**
 * Created by mharkins on 8/21/14.
 */
public class Player {
    public static final String TAG = Player.class.getSimpleName();

    /**
     * Singleton
     */
    static Player mInstance;

    protected Bus mEventBus = SingleEventBus.getInstance();
    protected PlayerServiceListener mPrivateServiceListener;

    // PLayer Listener
    private PlayerListener mPlayerListener;
    private NavListener mNavListener;
    private SocialListener mSocialListener;

    protected Player(Context context, PlayerListener playerListener, NavListener navListener, SocialListener socialListener) {
        setPlayerListener(playerListener);
        setNavListener(navListener);
        setSocialListener(socialListener);

        mPrivateServiceListener = new PlayerServiceListener();
        mEventBus.register(mPrivateServiceListener);

        startPlayerService(context);
    }

    protected void startPlayerService(Context context) {
        // Start the Service
        Intent intent = new Intent(context, PlayerService.class);
        context.startService(intent);
    }

    public static Player getInstance(Context context, PlayerListener playerListener, NavListener navListener, SocialListener socialListener) {
        if (mInstance == null) {
            mInstance = new Player(context, playerListener, navListener, socialListener);
        }
        return mInstance;
    }

    public void setPlayerListener(PlayerListener mPlayerListener) {
        this.mPlayerListener = mPlayerListener;
    }

    public void setSocialListener(SocialListener mSocialListener) {
        this.mSocialListener = mSocialListener;
    }

    public void setNavListener(NavListener mNavListener) {
        this.mNavListener = mNavListener;
    }

    public void setCredentials(String token, String secret) {
        Credentials credentials = new Credentials(token, secret);
        if (credentials.isValid()) {
            mEventBus.post(credentials);
        }
    }

    public void setPlacementId(int placementId) {
        mEventBus.post(new Placement(placementId));
    }

    public void setStationId(String stationId) {
        mEventBus.post(new OutStationWrap(new Station(stationId)));
    }

    public void tune() {
        mEventBus.post(new PlayerAction(PlayerAction.ActionType.TUNE));
    }

    public void play() {
        mEventBus.post(new PlayerAction(PlayerAction.ActionType.PLAY));
    }

    public void pause() {
        mEventBus.post(new PlayerAction(PlayerAction.ActionType.PAUSE));
    }

    public void skip() {
        mEventBus.post(new PlayerAction(PlayerAction.ActionType.SKIP));
    }

    public void like() {
        mEventBus.post(new PlayerAction(PlayerAction.ActionType.LIKE));
    }

    public void dislike() {
        mEventBus.post(new PlayerAction(PlayerAction.ActionType.DISLIKE));
    }

    public void unlike() {
        mEventBus.post(new PlayerAction(PlayerAction.ActionType.UNLIKE));
    }


    // TODO: find a way to make this private and not break the Unit Tests
    public class PlayerServiceListener {
        public PlayerServiceListener() {
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onServiceReady(PlayerLibraryInfo playerLibraryInfo) {
            if (mPlayerListener != null) mPlayerListener.onPlayerInitialized(playerLibraryInfo);
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onServiceStatusChange(EventMessage message) {
            switch (message.getStatus()) {
                case SKIP_FAILED:
                    if (mNavListener != null) mNavListener.onSkipFailed();
                case LIKE:
                    if (mSocialListener != null) mSocialListener.onLiked();
                    break;
                case UNLIKE:
                    if (mSocialListener != null) mSocialListener.onUnliked();
                    break;
                case DISLIKE:
                    if (mSocialListener != null) mSocialListener.onDisliked();
                    break;
                default:
                    break;
            }
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onPlacementChanged(Pair<Placement, List<Station>> placementInfo) {
            if (mNavListener != null) mNavListener.onPlacementChanged(placementInfo.first, placementInfo.second);
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onStationChanged(Station station) {
            if (mNavListener != null) mNavListener.onStationChanged(station);
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onTrackChanged(Play play) {
            if (mNavListener != null) mNavListener.onTrackChanged(play);
        }



        @SuppressWarnings("unused")
        @Subscribe
        public void onBufferUpdate(BufferUpdate update) {
            if (mNavListener != null) mNavListener.onBufferUpdate(update.getPlay(), update.getPercentage());
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onProgressUpdate(ProgressUpdate update) {
            if (mNavListener != null) mNavListener.onProgressUpdate(update.getPlay(), update.getElapsedTime(), update.getTotalTime());
        }
    }

    /**
     * Implement this interface to get callbacks from the Player
     */
    public interface PlayerListener {
        public void onPlayerInitialized(PlayerLibraryInfo playerLibraryInfo);

        /**
         * Called when the user is not located in the US. No music will be available to play.
         */
        public void onNotInUS();
    }

    /**
     * Implement this interface to get callbacks from the Player
     */
    public interface NavListener {
        public void onPlacementChanged(Placement placement, List<Station> stationList);

        public void onStationChanged(Station station);

        public void onTrackChanged(Play play);

        public void onPlaybackStateChanged(Placement placement, List<Station> stationList);

        public void onSkipFailed();

        /**
         * Called when the user is not located in the US. No music will be available to play.
         */
        public void onNotInUS();

        public void onBufferUpdate(Play play, int percentage);

        public void onProgressUpdate(Play play, int elapsedTime, int totalTime);
    }

    public interface SocialListener {
        public void onLiked();
        public void onUnliked();
        public void onDisliked();
    }
}
