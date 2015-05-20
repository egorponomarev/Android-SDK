package fm.feed.android.playersdk;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.PlayInfo;
import fm.feed.android.playersdk.service.PlayerService;
import fm.feed.android.playersdk.service.bus.BufferUpdate;
import fm.feed.android.playersdk.service.bus.BusProvider;
import fm.feed.android.playersdk.service.bus.ChangeStation;
import fm.feed.android.playersdk.service.bus.Credentials;
import fm.feed.android.playersdk.service.bus.EventMessage;
import fm.feed.android.playersdk.service.bus.LogEvent;
import fm.feed.android.playersdk.service.bus.ChangeNotificationBuilder;
import fm.feed.android.playersdk.service.bus.PlayerAction;
import fm.feed.android.playersdk.service.bus.ProgressUpdate;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 **/

/**
 *
 * Top Level Interface controlling Feed Media Playback
 * *
 * <h1>Class Overview</h1>
 *
 * To get started, set the authentication tokens in your app as early as possible
 * (in Application.onCreate would be best, but you could also do this in your
 * Activity.onCreate as well):
 *
 * <pre>
 *     <code>
 *         Player.setTokens(getContext(), "demo", "demo");
 *     </code>
 * </pre>
 *
 * The player will initialize right after that call, and try to contact Feed.fm to get
 * information about the available stations and confirm that the client may
 * receive music. Until the player gets that response, there isn't anything it
 * will do, aside from register event listeners.
 *
 * To be notified when the player has either successfully contacted Feed.fm and confirmed
 * it can play back music, or when the player is unable to get permission from Feed.fm
 * to play music, use the onPlayerAvailability() method:
  *
 * <pre>
 *     <code>
 *         Player player = Player.getInstance();
 *
 *         player.onPlayerAvailability(new PlayerAvailabilityListener() {
 *            public void onAvailable() {
 *                // player is ready for playback
 *
 *                // enable player UI here
 *
 *                // start loading a song in the background for immediate future playback:
 *                player.tune();
 *
 *                // ... or load and start a song immediately:
 *                player.play();
 *            }
 *
 *            public void onUnavailable() {
 *                // Player is not licensed for playback in this area or an
 *                // error occurred on initialization. Perhaps hide the player
 *                // or turn on local music.
 *            }
 *         });
 *     </code>
 * </pre>
 *
 * </p>
 */

public class Player {
    public static final String TAG = Player.class.getSimpleName();

    /**
     * Singleton
     */
    private static Player mInstance;

    private PlayerService.BuildType mDebug = PlayerService.BuildType.DEBUG;

    protected PlayerServiceListener mPlayerServiceListener;
    protected Bus mEventBus;

    // PLayer Listener
    private List<PlayerListener> mPlayerListeners = new ArrayList<PlayerListener>();
    private List<NavListener> mNavListeners = new ArrayList<NavListener>();
    private List<SocialListener> mSocialListeners = new ArrayList<SocialListener>();
    private List<PlayerAvailabilityListener> mPlayerAvailabilityListeners = new ArrayList<PlayerAvailabilityListener>();

    // Queue up logging events until we've got something to send them out
    private List<LogEvent> mLogEvents = new ArrayList<LogEvent>();

    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private PlayInfo mPlayInfo;

    private Credentials mCredentials;

    /*
     * Save credentials and create and pass on to new PlayerService
     */

    private Player(Context context, Credentials credz) {
        mCredentials = credz;

        mEventBus = BusProvider.getInstance();

        mPlayerServiceListener = new PlayerServiceListener();
        mEventBus.register(mPlayerServiceListener);

        Intent intent = new Intent(context, PlayerService.class);
        intent.putExtra(PlayerService.ExtraKeys.timestamp.toString(), new Date().getTime());
        intent.putExtra(PlayerService.ExtraKeys.buildType.toString(), mDebug.name());
        intent.putExtra(PlayerService.ExtraKeys.token.toString(), credz.getToken());
        intent.putExtra(PlayerService.ExtraKeys.secret.toString(), credz.getSecret());

        context.startService(intent);
    }

    /*
     * Assign authentication tokens for talking to the Feed.fm service and kick off
     * a Service that does the talking. This should be called before getInstance()
     * and should never be called with different token/secret values during the lifetime
     * of the app. Multiple calls won't have any effect. This should be called as early
     * in the life of your app as possible.
     */

    public static void setTokens(Context context, String token, String secret) {
        if (mInstance != null) {
            Credentials credz = mInstance.mCredentials;

            if (!token.equals(credz.getToken()) || !secret.equals(credz.getSecret())) {
                Log.e(TAG, "Attempted to set token and secret to " + token + " and " + secret + ", but they are already set to " + credz.getToken() + " and " + credz.getSecret());
            }

        } else {
            Credentials credz = new Credentials(token, secret);

            if (!credz.isValid()) {
                Log.e(TAG, "Invalid credentials were passed to Player.setTokens()");

            } else {
                mInstance = new Player(context, credz);
            }

        }
    }

    /**
     * Get a Singleton instance of the {@link fm.feed.android.playersdk.Player}. First
     * make sure {@link fm.feed.android.playersdk.Player#setTokens} has been called.
     *
     * @return
     */
    public static Player getInstance() {
        if (mInstance == null) {
            Log.e(TAG, "A Player.getInstance() call was made before Player.setTokens() was called!");
        }

        return mInstance;
    }

    /*
     * Music can continue to play while our containing app is 'background'ed so, by
     * default, we create a Notification that shows the currently playing song and
     * gives the user a way to return back to the app to interact with the player.
     * This method lets you specify your own class that will style the Notification
     * that is shown during music playback.
     *
     * A null builder will prevent the library from creating Notifications.
     */

    public void setNotificationBuilder(NotificationBuilder nb) {
        mEventBus.post(new ChangeNotificationBuilder(nb));
    }

    /*
     * After the Player is created, it must contact the feed.fm service to confirm
     * that the client may play music. If the services responds in the affirmative, then
     * the 'onAvailable()' method is executed, else the 'onUnavailable()' method is
     * executed.  The state of the player is remembered, so this method can be called
     * well after the player has started.
     *
     * The callback methods are always executed asynchronously.
     */

    public void onPlayerAvailability(PlayerAvailabilityListener pal) {
        mPlayerAvailabilityListeners.add(pal);

        if (mPlayInfo != null) {
            sendPlayerAvailability(mPlayInfo.getState() != PlayInfo.State.UNAVAILABLE);

        } // else waiting for initialization
    }

    private void sendPlayerAvailability(final boolean available) {
        // always posted to the run loop rather than sometimes immediate and sometimes async.
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                while (mPlayerAvailabilityListeners.size() > 0) {
                    PlayerAvailabilityListener pal = mPlayerAvailabilityListeners.remove(0);

                    if (available) {
                        pal.onAvailable();
                    } else {
                        pal.onUnavailable();
                    }
                }
            }
        });
    }

    public void registerPlayerListener(PlayerListener playerListener) {
        if (!mPlayerListeners.contains(playerListener)) {
            mPlayerListeners.add(playerListener);
        }
    }

    public void unregisterPlayerListener(PlayerListener playerListener) {
        mPlayerListeners.remove(playerListener);
    }


    public void registerSocialListener(SocialListener socialListener) {
        if (!mSocialListeners.contains(socialListener)) {
            mSocialListeners.add(socialListener);
        }
    }

    public void unregisterSocialListener(SocialListener socialListener) {
        mSocialListeners.remove(socialListener);
    }


    public void registerNavListener(NavListener navListener) {
        if (!mNavListeners.contains(navListener)) {
            mNavListeners.add(navListener);
        }
    }

    public void unregisterNavListener(NavListener navListener) {
        mNavListeners.remove(navListener);
    }

    /**
     * Returns whether or not the PlayerService is initialized and available for
     * playback.
     * <p>
     * If not ready yet; the Player will not have the {@link fm.feed.android.playersdk.service.PlayInfo} initialized or the {@link fm.feed.android.playersdk.service.PlayInfo.State} will be UNAVAILABLE.
     * </p>
     *
     * @return {@code true} if initialized, {@code false} otherwise.
     */

    public boolean isAvailable() {
        return (mPlayInfo != null) && (mPlayInfo.getState() != PlayInfo.State.UNAVAILABLE);
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
        if (isAvailable()) {
            mEventBus.post(new ChangeStation(new Station(stationId)));
        }
    }

    /**
     * Causes the first audio file to start loading
     * <p>
     * No audio playback commences.
     * </p>
     */
    public void tune() {
        if (isAvailable()) {
            mEventBus.post(new PlayerAction(PlayerAction.ActionType.TUNE));
        }
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
        if (isAvailable()) {
            mEventBus.post(new PlayerAction(PlayerAction.ActionType.PLAY));
        }
    }

    /**
     * Pauses playback
     */
    public void pause() {
        if (isAvailable()) {
            mEventBus.post(new PlayerAction(PlayerAction.ActionType.PAUSE));
        }
    }

    /**
     * Indicates that the user would like to skip the current track
     * <p>If allowed, playback of the current song will stop and move on to the next song.</p>
     * <p> Check on {@link fm.feed.android.playersdk.Player#isSkippable()} to know if the user can skip the track.</p>
     */
    public void skip() {
        if (isAvailable()) {
            mEventBus.post(new PlayerAction(PlayerAction.ActionType.SKIP));
        }
    }

    /**
     * Indicates to the server that the user likes the current song
     */
    public void like() {
        if (isAvailable()) {
            mEventBus.post(new PlayerAction(PlayerAction.ActionType.LIKE));
        }
    }

    /**
     * Indicates to the server that the user no longer likes the current song
     */
    public void unlike() {
        if (isAvailable()) {
            mEventBus.post(new PlayerAction(PlayerAction.ActionType.UNLIKE));
        }
    }

    /**
     * Indicates to the server that the user doesn't like the current song
     * <p>Initiates a {@link fm.feed.android.playersdk.Player#skip()}</p>
     */
    public void dislike() {
        if (isAvailable()) {
            mEventBus.post(new PlayerAction(PlayerAction.ActionType.DISLIKE));
        }
    }

    /**
     * List of {@link fm.feed.android.playersdk.model.Station}s for the current {@link fm.feed.android.playersdk.model.Placement}
     *
     * @return The list of {@link fm.feed.android.playersdk.model.Station}s for the current {@link fm.feed.android.playersdk.model.Placement}.
     */
    public List<Station> getStationList() {
        return isAvailable() ? mPlayInfo.getStationList() : null;
    }

    /**
     * Currently selected {@link fm.feed.android.playersdk.model.Station}
     *
     * @return The current {@link fm.feed.android.playersdk.model.Station}
     */
    public Station getStation() {
        return isAvailable() ? mPlayInfo.getStation() : null;
    }


    public boolean hasStationList() {
        return isAvailable() && mPlayInfo.getStationList() != null;
    }

    public List<Play> getPlayHistory() {
        return isAvailable() ? mPlayInfo.getPlayHistory() : Collections.<Play>emptyList();
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
        return isAvailable() && mPlayInfo.getPlay() != null;
    }

    /**
     * Current track ({@link fm.feed.android.playersdk.model.Play}) being played
     *
     * @return The current {@link fm.feed.android.playersdk.model.Play}.
     */
    public Play getPlay() {
        return isAvailable() ? mPlayInfo.getPlay() : null;
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
        return isAvailable() && mPlayInfo.isSkippable();
    }

    /**
     * {@link fm.feed.android.playersdk.service.PlayInfo.State} of the Player.
     *
     * @return The current {@link fm.feed.android.playersdk.service.PlayInfo.State}.
     */
    public PlayInfo.State getState() {
        return isAvailable() ? mPlayInfo.getState() : null;
    }

    /**
     * Send an event to the Feed.fm server for logging
     */

    public void logEvent(String event, String ... parameters) {
        LogEvent le;

        if (parameters.length > 0) {
            Map<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < parameters.length - 1; i += 2) {
                map.put(parameters[i], parameters[i+1]);
            }

            le = new LogEvent(event, map);

        } else {
            le = new LogEvent(event);
        }

        if (mPlayInfo == null) {
            mLogEvents.add(le);

        } else {
            mEventBus.post(le);
        }

    }

    /**
     * Tell the feed.fm servers that the app has started.
     *
     * This is used for computing session times with and without music.
     */

    public void logLaunched() {
        logEvent("launched", "from", "tokens");
    }

    /**
     * Tell the feedfm servers that the app resumed after the user switched
     * to another app.
     *
     * This is used for computing session times with and without music.
     */

    public void logResumed() {
        logEvent("launched", "from", "enter foreground");
    }

    /**
     * Tell the feed.fm servers that the app was put in the background.
     *
     * This is used for computing session times with and without music.
     */

    public void logBackgrounded() {
        boolean playing = ((mPlayInfo != null) && (mPlayInfo.getState() == PlayInfo.State.PLAYING));

        logEvent("backgrounded", "playing", "" + playing);
    }


    /**
     * This class watches the bus for events from the PlayerService
     */

    public class PlayerServiceListener {

        @SuppressWarnings("unused")
        @Subscribe
        public void onServiceReady(PlayInfo playInfo) {
            mPlayInfo = playInfo;

            boolean available = (mPlayInfo.getState() != PlayInfo.State.UNAVAILABLE);

            sendPlayerAvailability(available);

            if (available) {
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (PlayerListener listener : mPlayerListeners) {
                            listener.onPlayerInitialized(mPlayInfo);
                        }
                    }
                });
            }

            // pass on any queued up log events
            while (mLogEvents.size() > 0) {
                LogEvent event = mLogEvents.remove(0);
                mEventBus.post(event);
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

}
