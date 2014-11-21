package fm.feed.android.playersdk;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
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
import fm.feed.android.playersdk.service.bus.OutNotificationBuilder;
import fm.feed.android.playersdk.service.bus.OutPlacementWrap;
import fm.feed.android.playersdk.service.bus.OutStationWrap;
import fm.feed.android.playersdk.service.bus.PlayerAction;
import fm.feed.android.playersdk.service.bus.ProgressUpdate;
import fm.feed.android.playersdk.service.constant.PlayerErrorEnum;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Created by mharkins on 8/21/14.
 *
 */

/**
 * Interface controlling the Feed Media Web-radio
 * <h1>Class Overview</h1>
 * To get started:
 * <pre>
 * <code> final Player player = Player.getInstance(getContext(), new Player.PlayerListener() {
 *
 *      {@literal @}Override public void onPlayerInitialized(PlayInfo playInfo) {
 *          player.play();
 *      }
 *
 *      {@literal @}Override public void onPlaybackStateChanged(PlayInfo.State state) {
 *          // Called when the playback changes state
 *      }
 *
 *      {@literal @}Override public void onError(PlayerError playerError) {
 *          // Called when there is an error
 *      }
 *
 *      {@literal @}Override public void onNotificationWillShow(int notificationId) {
 *          // Called when the Foreground Service notification is created
 *      }
 *  }, AUTH_TOKEN, AUTH_SECRET, CUSTOM_NOTIFICATION_ID);
 * </code></pre>
 * </p>
 */
public class Player {
    public static final String TAG = Player.class.getSimpleName();

    /**
     * Singleton
     */
    private static Player mInstance;

    private PlayerService.BuildType mDebug = PlayerService.BuildType.DEBUG;

    protected PlayerServiceListener mPrivateServiceListener;
    protected Bus mEventBus;

    // PLayer Listener
    private List<PlayerListener> mPlayerListeners = new ArrayList<PlayerListener>();
    private List<NavListener> mNavListeners = new ArrayList<NavListener>();
    private List<SocialListener> mSocialListeners = new ArrayList<SocialListener>();

    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private PlayInfo mPlayInfo;

    private Credentials mCredentials;

    private boolean mRequiresAuthentication;

    protected Player(Context context, Bus bus, String token, String secret) {
        mEventBus = bus;
        mRequiresAuthentication = false;

        mPrivateServiceListener = new PlayerServiceListener();
        mEventBus.register(mPrivateServiceListener);

        mCredentials = new Credentials(token, secret);

        startPlayerService(context);
    }

    protected void startPlayerService(Context context) {
        // Start the Service
        Intent intent = new Intent(context, PlayerService.class);
        intent.putExtra(PlayerService.ExtraKeys.timestamp.toString(), new Date().getTime());
        intent.putExtra(PlayerService.ExtraKeys.buildType.toString(), mDebug.name());

        context.startService(intent);
    }

    /**
     * Get a Singleton instance of the {@link fm.feed.android.playersdk.Player}.
     *
     * @param context
     *
     * @return
     */
    public static Player getInstance(Context context, PlayerListener playerListener, String token, String secret) {
        if (mInstance == null) {
            mInstance = new Player(context, BusProvider.getInstance(), token, secret);
        }
        mInstance.registerPlayerListener(playerListener);
        return mInstance;
    }

    public void registerPlayerListener(PlayerListener playerListener) {
        if (!mPlayerListeners.contains(playerListener)) {
            mPlayerListeners.add(playerListener);
        }
    }

    public void registerSocialListener(SocialListener socialListener) {
        if (!mSocialListeners.contains(socialListener)) {
            mSocialListeners.add(socialListener);
        }
    }

    public void registerNavListener(NavListener navListener) {
        if (!mNavListeners.contains(navListener)) {
            mNavListeners.add(navListener);
        }
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

    /**
     * Returns whether or not the PlayerService is initialized yet.
     * <p>
     * If not ready yet; the Player will not have the {@link fm.feed.android.playersdk.service.PlayInfo} initialized; and the {@link fm.feed.android.playersdk.service.PlayInfo.State} will be null.
     * </p>
     *
     * @return {@code true} if initialized, {@code false} otherwise.
     */
    public boolean isInitialized() {
        return mPlayInfo != null;
    }

    /**
     * Assigns to the player the token and secret credentials needed for communicating with the service
     * <p>
     * This call is required for any use of the service.<br/>
     * It should be called after {@link fm.feed.android.playersdk.Player.PlayerListener#onPlayerInitialized(fm.feed.android.playersdk.service.PlayInfo)} has been called.<br/>
     * </p>
     * <p>
     * Setting the credentials will download your default {@link fm.feed.android.playersdk.model.Placement} from the server; containing the list of your {@link Station}s.
     * </p>
     * <p>
     * The {@code token} and {@code secret} are available through your Feed.FM account:
     * <ol>
     * <li>Select an App in <b><a href="http://developer.feed.fm/dashboard">Your Apps and Websites</a></b></li>
     * <li>Go to tab <b>Developer Codes and IDs</b></li>
     * <li>Save the {@code token} and {@code secret} statically in your app.</li>
     * </ol>
     * </p>
     *
     * @param token
     *         Generated when your Feed.FM app is created on your Feed.FM dashboard.
     * @param secret
     *         Generated when your Feed.FM app is created on your Feed.FM dashboard.
     */
    public void setCredentials(String token, String secret) {
        Credentials credentials = new Credentials(token, secret);
        if (credentials.isValid()) {
            mEventBus.post(credentials);
        } else {
            PlayerError playerError = new PlayerError(PlayerErrorEnum.INVALID_CREDENTIALS);
            for (PlayerListener listener : mPlayerListeners) {
                listener.onError(playerError);
            }
        }
    }

    /**
     * Tells the server which placement to pull playable stations from
     * <p>Will start playback automatically once the {@link fm.feed.android.playersdk.model.Placement} information has been retrieved.</p>
     * <p>
     * This is an optional call, as a default placement is associated with the authentication credentials.
     * </p>
     * <p>
     * The {@code placementId} is available through your Feed.FM account:
     * <ol>
     * <li>Select an App in <b><a href="http://developer.feed.fm/dashboard">Your Apps and Websites</a></b></li>
     * <li>Go to tab <b>Developer Codes and IDs</b></li>
     * <li>Use the <b>placement ID</b> as a parameter</li>
     * </ol>
     * </p>
     *
     * @param placementId
     *         The placement ID for this App
     */
    public void setPlacementId(Integer placementId) {
        mEventBus.post(new OutPlacementWrap(new Placement(placementId)));
    }

    /**
     * Tells the server which station within the current placement to pull playable stations from
     * <p>Will start playback automatically.</p>
     * <p>
     * This is an optional call, as a default station is associated with the current placement.
     * </p>
     * <p>
     * The {@code stationId} can be found in a {@link fm.feed.android.playersdk.model.Station} object ({@link fm.feed.android.playersdk.model.Station#getId()}).
     * </p>
     *
     * @param stationId
     *         The station identifier to play music from.
     */
    public void setStationId(Integer stationId) {
        mEventBus.post(new OutStationWrap(new Station(stationId)));
    }

    /**
     * Causes the first audio file to start loading
     * <p>
     * No audio playback commences.
     * </p>
     */
    public void tune() {
        mEventBus.post(new PlayerAction(PlayerAction.ActionType.TUNE));
    }

    /**
     * Loads and starts or resumes a playback
     * <p>
     * <ul>
     * <li>If the {@link fm.feed.android.playersdk.Player} state is {@link fm.feed.android.playersdk.service.PlayInfo.State#READY}, will tune and then play the recording.</li>
     * <li>If the {@link fm.feed.android.playersdk.Player} state is {@link fm.feed.android.playersdk.service.PlayInfo.State#TUNED}, will start playback</li>
     * <li>If the {@link fm.feed.android.playersdk.Player} state is {@link fm.feed.android.playersdk.service.PlayInfo.State#PAUSED}, will resume playback</li>
     * </ul>
     * </p>
     */
    public void play() {
        mEventBus.post(new PlayerAction(PlayerAction.ActionType.PLAY));
    }

    /**
     * Pauses playback
     */
    public void pause() {
        mEventBus.post(new PlayerAction(PlayerAction.ActionType.PAUSE));
    }

    /**
     * Indicates that the user would like to skip the current track
     * <p>If allowed, playback of the current song will stop and move on to the next song.</p>
     * <p> Check on {@link fm.feed.android.playersdk.Player#isSkippable()} to know if the user can skip the track.</p>
     */
    public void skip() {
        mEventBus.post(new PlayerAction(PlayerAction.ActionType.SKIP));
    }

    /**
     * Indicates to the server that the user likes the current song
     */
    public void like() {
        mEventBus.post(new PlayerAction(PlayerAction.ActionType.LIKE));
    }

    /**
     * Indicates to the server that the user no longer likes the current song
     */
    public void unlike() {
        mEventBus.post(new PlayerAction(PlayerAction.ActionType.UNLIKE));
    }

    /**
     * Indicates to the server that the user doesn't like the current song
     * <p>Initiates a {@link fm.feed.android.playersdk.Player#skip()}</p>
     */
    public void dislike() {
        mEventBus.post(new PlayerAction(PlayerAction.ActionType.DISLIKE));
    }

    /**
     * Current {@link fm.feed.android.playersdk.model.Placement} information
     *
     * @return The current {@link fm.feed.android.playersdk.model.Placement} information.
     */
    public Placement getPlacement() {
        return mPlayInfo != null ? mPlayInfo.getPlacement() : null;
    }

    /**
     * List of {@link fm.feed.android.playersdk.model.Station}s for the current {@link fm.feed.android.playersdk.model.Placement}
     *
     * @return The list of {@link fm.feed.android.playersdk.model.Station}s for the current {@link fm.feed.android.playersdk.model.Placement}.
     */
    public List<Station> getStationList() {
        return mPlayInfo != null ? mPlayInfo.getStationList() : null;
    }

    /**
     * Currently selected {@link fm.feed.android.playersdk.model.Station}
     *
     * @return The current {@link fm.feed.android.playersdk.model.Station}
     */
    public Station getStation() {
        return mPlayInfo != null ? mPlayInfo.getStation() : null;
    }


    public boolean hasStationList() {
        return mPlayInfo != null && mPlayInfo.getStationList() != null;
    }

    public List<Play> getPlayHistory() {
        return (mPlayInfo == null) ? Collections.<Play>emptyList() : mPlayInfo.getPlayHistory();
    }

    public void setMaxPlayHistorySize(int newMaxHistoryLength) {
        PlayInfo.setMaxPlayHistorySize(newMaxHistoryLength);
    }

    /**
     * Checks if there is currently a {@link fm.feed.android.playersdk.model.Play}
     *
     * @return {@code true} if there is a current {@link fm.feed.android.playersdk.model.Play}, {@code false} otherwise.
     */
    public boolean hasPlay() {
        return mPlayInfo != null && mPlayInfo.getPlay() != null;
    }

    /**
     * Current track ({@link fm.feed.android.playersdk.model.Play}) being played
     *
     * @return The current {@link fm.feed.android.playersdk.model.Play}.
     */
    public Play getPlay() {
        return mPlayInfo != null ? mPlayInfo.getPlay() : null;
    }

    /**
     * Can this {@link Play} be skipped
     * <p>
     * A user can only skip tracks a certain number of times. Depending on server/station rules.
     * </p>
     *
     * @return {@code true} if track can be skipped, {@code false} otherwise.
     */
    public boolean isSkippable() {
        return mPlayInfo != null && mPlayInfo.isSkippable();
    }

    /**
     * {@link fm.feed.android.playersdk.service.PlayInfo.State} of the Player.
     *
     * @return The current {@link fm.feed.android.playersdk.service.PlayInfo.State}.
     */
    public PlayInfo.State getState() {
        return mPlayInfo != null ? mPlayInfo.getState() : null;
    }

    // TODO: find a way to make this private and not break the Unit Tests
    public class PlayerServiceListener {
        public PlayerServiceListener() {
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onServiceReady(PlayInfo playInfo) {
            mPlayInfo = playInfo;

            mRequiresAuthentication = !mPlayInfo.hasCredentials();

            NotificationBuilder notificationBuilder = mPlayerListeners.isEmpty() ? null : mPlayerListeners.get(0).getNotificationBuilder();
            if (notificationBuilder != null) {
                mEventBus.post(new OutNotificationBuilder(notificationBuilder));
            }

            if (mRequiresAuthentication) {
                setCredentials(mCredentials.getToken(), mCredentials.getSecret());
            } else {
                for (PlayerListener listener : mPlayerListeners) {
                    listener.onPlayerInitialized(playInfo);
                }
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
                            break;
                        case SKIP_STATUS_UPDATED:
                            for (PlayerListener listener : mPlayerListeners) {
                                listener.onSkipStatusChange(isSkippable());
                            }
                            break;
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
                        default:
                            break;

                    }
                }
            });
        }

        @SuppressWarnings("unused")
        @Subscribe
        public void onPlacementChanged(final Placement placement) {
            if (mRequiresAuthentication) {
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (PlayerListener listener : mPlayerListeners) {
                            listener.onPlayerInitialized(mPlayInfo);
                        }
                    }
                });
                mRequiresAuthentication = false;
            }

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

        @SuppressWarnings("unused")
        @Subscribe
        public void onError(final FeedFMError error) {
            // TODO: filter errors a bit.
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (PlayerListener listener : mPlayerListeners) {
                        listener.onError(new PlayerError(error));
                    }
                }
            });
        }
    }

    /**
     * Implement this interface to get callbacks from the Player
     */
    public interface PlayerListener {

        /**
         * Called when the {@link Player} is initialized and authenticated with the server
         *
         * @param playInfo
         *         The {@link fm.feed.android.playersdk.service.PlayInfo} object containing the play state as well as other information pertaining to the library.
         */
        public void onPlayerInitialized(PlayInfo playInfo);

        /**
         * The {@link fm.feed.android.playersdk.Player.NotificationBuilder} that will handle creating the play notifications.
         *
         * @return The {@link fm.feed.android.playersdk.Player.NotificationBuilder} that will handle creating the play notifications.
         * If {@code null} the radio will not keep playing when the application is killed.
         */
        public NotificationBuilder getNotificationBuilder();

        /**
         * Called when the playback state changes
         * <p>
         * Can be one of:
         * <ul>
         * <li>{@link fm.feed.android.playersdk.service.PlayInfo.State#WAITING} - Player is Waiting for Metadata from the Server</li>
         * <li>{@link fm.feed.android.playersdk.service.PlayInfo.State#READY} - Player is ready to play music</li>
         * <li>{@link fm.feed.android.playersdk.service.PlayInfo.State#PAUSED} - Audio playback is currently paused</li>
         * <li>{@link fm.feed.android.playersdk.service.PlayInfo.State#PLAYING} - Audio playback is currently processing</li>
         * <li>{@link fm.feed.android.playersdk.service.PlayInfo.State#STALLED} - Audio Playback has paused due to lack of audio data from the server</li>
         * <li>{@link fm.feed.android.playersdk.service.PlayInfo.State#COMPLETE} - The player has run out of available music to play</li>
         * <li>{@link fm.feed.android.playersdk.service.PlayInfo.State#REQUESTING_SKIP} - The player is waiting for the server to say if the current song can be skipped</li>
         * </ul>
         * </p>
         *
         * @param state
         *         {@link fm.feed.android.playersdk.service.PlayInfo.State} of the Player.
         */
        public void onPlaybackStateChanged(PlayInfo.State state);

        /**
         * Called when the Skip status has changed.
         * <p>
         * Users can only skip an X number of times within a certain timeperiod
         * </p>
         * <p>
         * Skip status can also be accessed with {@link Player#isSkippable()}
         * </p>
         *
         * @param skippable
         *         Skip Status
         */
        public void onSkipStatusChange(boolean skippable);

        /**
         * Called when there is an unhandled error.
         *
         * @param playerError
         */
        public void onError(PlayerError playerError);
    }

    /**
     * Implement this interface to get callbacks from the Player
     */
    public interface NavListener {
        /**
         * Called when the placement has changed
         *
         * @param placement
         *         The currently selected {@link fm.feed.android.playersdk.model.Placement}
         * @param stationList
         *         The new list of {@link fm.feed.android.playersdk.model.Station}s for this {@link fm.feed.android.playersdk.model.Placement}
         */
        public void onPlacementChanged(Placement placement, List<Station> stationList);

        /**
         * Called when the new {@link fm.feed.android.playersdk.model.Station} has been set
         *
         * @param station
         *         The currently selected {@link fm.feed.android.playersdk.model.Station}.
         */
        public void onStationChanged(Station station);

        /**
         * Called when a new {@link fm.feed.android.playersdk.model.Play} has started buffering
         *
         * @param play
         *         The new {@link fm.feed.android.playersdk.model.Play}
         */
        public void onTrackChanged(Play play);

        /**
         * Called when the user has reached the end of the selected {@link Station}
         */
        public void onEndOfPlaylist();

        /**
         * Called when a {@link Player#skip()} has failed
         */
        public void onSkipFailed();

        /**
         * Called repeatedly while the Audio is being buffered
         *
         * @param play
         *         The current {@link fm.feed.android.playersdk.model.Play}
         * @param percentage
         *         The percentage buffered
         */
        public void onBufferUpdate(Play play, int percentage);

        /**
         * Called repeatedly while the Audio is playing
         *
         * @param play
         *         The current {@link fm.feed.android.playersdk.model.Play}
         * @param elapsedTime
         *         How far along in the track the current {@link fm.feed.android.playersdk.model.Play} is.
         * @param totalTime
         *         The duration of the track.
         */
        public void onProgressUpdate(Play play, int elapsedTime, int totalTime);
    }

    /**
     * Social related events
     */
    public interface SocialListener {
        /**
         * Called when the song has been liked
         */
        public void onLiked();

        /**
         * Called when the song has been unliked
         */
        public void onUnliked();

        /**
         * Called when the song has been disliked
         */
        public void onDisliked();
    }

    /**
     * The notification builder used by the Service to enable foreground (running radio while app is killed)
     */
    public interface NotificationBuilder {
        /**
         * Called when the Service will show the Persistent notification with a new play
         * <p>
         * Provide your own implementation of the Notification based on the {@link fm.feed.android.playersdk.model.Play}
         * </p>
         * <p>
         * See <a href="http://developer.android.com/guide/components/services.html#Foreground">Running a Service in the Foreground</a> for more details.
         * </p>
         *
         * @param serviceContext the Service context.
         * @param play the current Play
         */
        public Notification build(Context serviceContext, Play play);

        /**
         * Called when the notification should disappear to allow the service to shut down.
         * @param serviceContext
         */
        public void destroy(Context serviceContext);

        public int getNotificationId();
    }
}
