package com.feedfm.android.playersdk;

import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import com.feedfm.android.playersdk.model.Placement;
import com.feedfm.android.playersdk.model.Station;
import com.feedfm.android.playersdk.service.bus.Credentials;
import com.feedfm.android.playersdk.service.bus.SingleEventBus;
import com.feedfm.android.playersdk.service.bus.StatusMessage;
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
    private static Player mInstance;

    private Bus eventBus = SingleEventBus.getInstance();
    private PlayerServiceListener mPrivateServiceListener;

    // Client Listener
    private ClientListener mClientListener;

    private Player(Context context, ClientListener clientListener) {
        mClientListener = clientListener;

        mPrivateServiceListener = new PlayerServiceListener();
        eventBus.register(mPrivateServiceListener);

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
            eventBus.post(credentials);
        }
    }

    public void setPlacementId(int placementId) {
        eventBus.post(new Placement(placementId));
    }

    public void setStationId(int stationId) {
        eventBus.post(new Station(stationId));
    }

    private class PlayerServiceListener {
        public PlayerServiceListener() {
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onServiceStatusChange(StatusMessage message) {
            switch (message.getStatus()) {
                case STARTED:
                    mClientListener.onPlayerInitialized();
                    break;
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

        public void onSkipFailed(Placement placement, List<Station> stationList);

        /**
         * Called when the user is not located in the US. No music will be available to play.
         */
        public void onNotInUS();
    }
}
