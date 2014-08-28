package com.feedfm.android.playersdk;

import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import com.feedfm.android.playersdk.model.Placement;
import com.feedfm.android.playersdk.model.Station;
import com.feedfm.android.playersdk.service.bus.Credentials;
import com.feedfm.android.playersdk.service.bus.EventMessage;
import com.feedfm.android.playersdk.service.bus.OutStationWrap;
import com.feedfm.android.playersdk.service.bus.PlayerAction;
import com.feedfm.android.playersdk.service.bus.SingleEventBus;
import com.feedfm.android.playersdk.service.webservice.PlayerService;
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

    // Client Listener
    private ClientListener mClientListener;

    protected Player(Context context, ClientListener clientListener) {
        mClientListener = clientListener;

        mPrivateServiceListener = new PlayerServiceListener();
        mEventBus.register(mPrivateServiceListener);

        startPlayerService(context);
    }

    protected void startPlayerService(Context context) {
        // Start the Service
        Intent intent = new Intent(context, PlayerService.class);
        context.startService(intent);
    }

    public static Player getInstance(Context context, ClientListener clientListener) {
        if (mInstance == null) {
            mInstance = new Player(context, clientListener);
        }
        return mInstance;
    }

    public ClientListener getPlayerListener() {
        return mClientListener;
    }

    public void setPlayerListener(ClientListener mClientListener) {
        this.mClientListener = mClientListener;
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
        public void onServiceStatusChange(EventMessage message) {
            switch (message.getStatus()) {
                case STARTED:
                    mClientListener.onPlayerInitialized();
                    break;
                case SKIP_FAILED:
                    mClientListener.onSkipFailed();
                default:
                    break;
            }
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onPlacementChanged(Pair<Placement, List<Station>> placementInfo) {
            mClientListener.onPlacementChanged(placementInfo.first, placementInfo.second);
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onStationChanged(Station station) {
            mClientListener.onStationChanged(station);
        }
    }

    /**
     * Implement this interface to get callbacks from the Player
     */
    public interface ClientListener {
        public void onPlayerInitialized();

        public void onPlacementChanged(Placement placement, List<Station> stationList);

        public void onStationChanged(Station station);

        public void onTrackChanged(Placement placement, List<Station> stationList);

        public void onPlaybackStateChanged(Placement placement, List<Station> stationList);

        public void onSkipFailed();

        /**
         * Called when the user is not located in the US. No music will be available to play.
         */
        public void onNotInUS();
    }
}
