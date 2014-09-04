package fm.feed.android.playersdk;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.PlayerLibraryInfo;
import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.PlayerService;
import fm.feed.android.playersdk.service.bus.BufferUpdate;
import fm.feed.android.playersdk.service.bus.BusProvider;
import fm.feed.android.playersdk.service.bus.Credentials;
import fm.feed.android.playersdk.service.bus.EventMessage;
import fm.feed.android.playersdk.service.bus.OutStationWrap;
import fm.feed.android.playersdk.service.bus.PlayerAction;
import fm.feed.android.playersdk.service.bus.ProgressUpdate;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
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

    protected Bus mEventBus = BusProvider.getInstance();
    protected PlayerServiceListener mPrivateServiceListener;

    // PLayer Listener
    private List<PlayerListener> mPlayerListeners = new ArrayList<PlayerListener>();
    private List<NavListener> mNavListeners = new ArrayList<NavListener>();
    private List<SocialListener> mSocialListeners = new ArrayList<SocialListener>();

    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    protected Player(Context context, PlayerListener playerListener, NavListener navListener, SocialListener socialListener) {
        registerPlayerListener(playerListener);
        registerNavListener(navListener);
        registerSocialListener(socialListener);

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

    public void registerPlayerListener(PlayerListener playerListener) {
        mPlayerListeners.add(playerListener);
    }

    public void registerSocialListener(SocialListener socialListener) {
        mSocialListeners.add(socialListener);
    }

    public void registerNavListener(NavListener navListener) {
        mNavListeners.add(navListener);
    }

    public void unregisterPlayerListener(PlayerListener playerListener) {
        mPlayerListeners.remove(playerListener);
    }

    public void unregisterSocialListener(SocialListener socialListener) {
        mSocialListeners.remove(socialListener);
    }

    public void unregisterNavListener(NavListener navListener) {
        mNavListeners.remove(navListener);
    }

    public void setCredentials(String token, String secret) {
        Credentials credentials = new Credentials(token, secret);
        if (credentials.isValid()) {
            mEventBus.post(credentials);
        }
    }

    public void setPlacementId(Integer placementId) {
        mEventBus.post(new Placement(placementId));
    }

    public void setStationId(Integer stationId) {
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
            for (PlayerListener listener: mPlayerListeners) {
                listener.onPlayerInitialized(playerLibraryInfo);
            }
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onServiceStatusChange(final EventMessage message) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {

                    switch (message.getStatus()) {
                        case SKIP_FAILED:
                            for (NavListener listener: mNavListeners) {
                                listener.onSkipFailed();
                            }
                        case LIKE:
                            for (SocialListener listener: mSocialListeners ) {
                                listener.onLiked();
                            }
                            break;
                        case UNLIKE:
                            for (SocialListener listener: mSocialListeners ) {
                                listener.onUnliked();
                            }
                            break;
                        case DISLIKE:
                            for (SocialListener listener: mSocialListeners ) {
                                listener.onDisliked();
                            }
                            break;
                        default:
                            break;

                    }
                }
            });
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onPlacementChanged(final Pair<Placement, List<Station>> placementInfo) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (NavListener listener: mNavListeners) {
                        listener.onPlacementChanged(placementInfo.first, placementInfo.second);
                    }
                }
            });
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onStationChanged(final Station station) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (NavListener listener: mNavListeners) {
                        listener.onStationChanged(station);
                    }
                }
            });
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onTrackChanged(final Play play) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (NavListener listener: mNavListeners) {
                        listener.onTrackChanged(play);
                    }
                }
            });
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onBufferUpdate(final BufferUpdate update) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (NavListener listener: mNavListeners) {
                        listener.onBufferUpdate(update.getPlay(), update.getPercentage());
                    }
                }
            });
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onProgressUpdate(final ProgressUpdate update) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (NavListener listener: mNavListeners) {
                        listener.onProgressUpdate(update.getPlay(), update.getElapsedTime(), update.getTotalTime());
                    }
                }
            });
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
