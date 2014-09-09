package fm.feed.android.playersdk;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.PlayInfo;
import fm.feed.android.playersdk.service.PlayerService;
import fm.feed.android.playersdk.service.bus.BufferUpdate;
import fm.feed.android.playersdk.service.bus.BusProvider;
import fm.feed.android.playersdk.service.bus.Credentials;
import fm.feed.android.playersdk.service.bus.EventMessage;
import fm.feed.android.playersdk.service.bus.OutPlacementWrap;
import fm.feed.android.playersdk.service.bus.OutStationWrap;
import fm.feed.android.playersdk.service.bus.PlayerAction;
import fm.feed.android.playersdk.service.bus.ProgressUpdate;

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

    private PlayInfo mPlayInfo;

    protected Player(Context context, int notificationId) {
        mPrivateServiceListener = new PlayerServiceListener();
        mEventBus.register(mPrivateServiceListener);

        startPlayerService(context, notificationId);
    }

    protected void startPlayerService(Context context, Integer notificationId) {
        // Start the Service
        Intent intent = new Intent(context, PlayerService.class);
        intent.putExtra(PlayerService.ExtraKeys.timestamp.toString(), new Date().getTime());

        if (notificationId >= -1) {
            intent.putExtra(PlayerService.ExtraKeys.notificationId.toString(), notificationId);
        }
        context.startService(intent);
    }

    public static Player getInstance(Context context) {
        return getInstance(context, null);
    }

    /**
     * Get a Singleton instance of the {@link fm.feed.android.playersdk.Player}.
     *
     * @param context
     * @param notificationId a custom ID for the notification that will be created when the Service runs in the foreground.
     * @return
     */
    public static Player getInstance(Context context, Integer notificationId) {
        if (mInstance == null) {
            mInstance = new Player(context, notificationId);
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
        mEventBus.post(new OutPlacementWrap(new Placement(placementId)));
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

    public Placement getPlacement() {
        return mPlayInfo != null ? mPlayInfo.getPlacement() : null;
    }

    public List<Station> getStationList() {
        return mPlayInfo != null ? mPlayInfo.getStationList() : null;
    }

    public boolean hasStationList() {
        return mPlayInfo != null && mPlayInfo.getStationList() != null;
    }

    public boolean hasPlay() {
        return mPlayInfo != null && mPlayInfo.getPlay() != null;
    }

    public Play getPlay() {
        return mPlayInfo != null ? mPlayInfo.getPlay() : null;
    }

    public int getNotificationId() {
        return mPlayInfo != null ? mPlayInfo.getNotificationId() : -1;
    }

    public boolean isSkippable() {
        return mPlayInfo != null ? mPlayInfo.isSkippable() : false;
    }


    // TODO: find a way to make this private and not break the Unit Tests
    public class PlayerServiceListener {
        public PlayerServiceListener() {
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onServiceReady(PlayInfo playInfo) {
            mPlayInfo = playInfo;

            for (PlayerListener listener : mPlayerListeners) {
                listener.onPlayerInitialized(playInfo);
            }
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onServiceStatusChange(final EventMessage message) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {

                    switch (message.getStatus()) {
                        case STATUS_UPDATED:
                            for (PlayerListener listener : mPlayerListeners) {
                                listener.onPlaybackStateChanged(mPlayInfo.getState());
                            }
                            break;
                        case SKIP_FAILED:
                            for (NavListener listener : mNavListeners) {
                                listener.onSkipFailed();
                            }
                        case LIKE:
                            for (SocialListener listener : mSocialListeners) {
                                listener.onLiked();
                            }
                            break;
                        case UNLIKE:
                            for (SocialListener listener : mSocialListeners) {
                                listener.onUnliked();
                            }
                            break;
                        case DISLIKE:
                            for (SocialListener listener : mSocialListeners) {
                                listener.onDisliked();
                            }
                            break;
                        case END_OF_PLAYLIST:
                            for (NavListener listener : mNavListeners) {
                                listener.onEndOfPlaylist();
                            }
                            break;
                        case NOT_IN_US:
                            for (PlayerListener listener : mPlayerListeners) {
                                listener.onNotInUS();
                            }
                            break;
                        case NOTIFICATION_WILL_SHOW:
                            for (PlayerListener listener : mPlayerListeners) {
                                listener.onNotificationWillShow(getNotificationId());
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
        public void onPlacementChanged(final Placement placement) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (NavListener listener : mNavListeners) {
                        listener.onPlacementChanged(placement, placement.getStationList());
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
                    for (NavListener listener : mNavListeners) {
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
                    for (NavListener listener : mNavListeners) {
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
                    for (NavListener listener : mNavListeners) {
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
                    for (NavListener listener : mNavListeners) {
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
        public void onPlayerInitialized(PlayInfo playInfo);

        public void onNotificationWillShow(int notificationId);

        public void onPlaybackStateChanged(PlayInfo.State state);

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

        public void onEndOfPlaylist();

        public void onSkipFailed();

        public void onBufferUpdate(Play play, int percentage);

        public void onProgressUpdate(Play play, int elapsedTime, int totalTime);
    }

    public interface SocialListener {
        public void onLiked();

        public void onUnliked();

        public void onDisliked();
    }
}
