package fm.feed.android.playersdk.service;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.Station;

/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * Created by mharkins on 8/29/14.
 */
public class PlayInfo {
    private static final String TAG = PlayInfo.class.getSimpleName();

    /**
     * Possible statuses for the Player.
     * <p/>
     * <ul>
     * <li>{@link fm.feed.android.playersdk.service.PlayInfo.State#WAITING} - Player is Waiting for Metadata from the Server</li>
     * <li>{@link fm.feed.android.playersdk.service.PlayInfo.State#READY} - Player is ready to play music</li>
     * <li>{@link fm.feed.android.playersdk.service.PlayInfo.State#PAUSED} - Audio playback is currently paused</li>
     * <li>{@link fm.feed.android.playersdk.service.PlayInfo.State#PLAYING} - Audio playback is currently processing</li>
     * <li>{@link fm.feed.android.playersdk.service.PlayInfo.State#STALLED} - Audio Playback has paused due to lack of audio data from the server</li>
     * <li>{@link fm.feed.android.playersdk.service.PlayInfo.State#COMPLETE} - The player has run out of available music to play</li>
     * <li>{@link fm.feed.android.playersdk.service.PlayInfo.State#REQUESTING_SKIP} - The player is waiting for the server to say if the current song can be skipped</li>
     * </ul>
     */
    public static enum State {
        /**
         * Player is Waiting for Metadata from the Server
         */
        WAITING,
        /**
         * Player is ready to play music
         */
        READY,
        /**
         * Audio Feed is getting ready
         */
        TUNING,
        /**
         * Audio Feed is ready for playback
         */
        TUNED,
        /**
         * Audio playback is currently paused
         */
        PAUSED,
        /**
         * Audio playback is currently processing
         */
        PLAYING,
        /**
         * Audio Playback has paused due to lack of audio data from the server
         */
        STALLED,
        /**
         * The player has run out of available music to play
         * <p>
         * {@link fm.feed.android.playersdk.Player#tune()} or {@link fm.feed.android.playersdk.Player#play()}
         * will have to be called again to try to get more music from the server.
         * </p>
         */
        COMPLETE,
        /**
         * The player is waiting for the server to say if the current song can be skipped
         */
        REQUESTING_SKIP
    }

    private String mSdkVersion;

    private String mClientId;

    private Placement mPlacement = null;
    private List<Station> mStationList;

    private State mState = null;
    private Station mStation = null;
    private Play mPlay = null;

    private boolean mSkippable;

    private static int maxPlayHistorySize = 10;
    private LinkedList<Play> playHistory = new LinkedList<Play>();

    protected PlayInfo(String sdkVersion) {
        this.mSdkVersion = sdkVersion;

        setState(State.WAITING);
    }

    // -----------------
    // Protected Accessors
    // -----------------

    protected String getClientId() {
        return mClientId;
    }

    protected void setClientId(String mClientId) {
        this.mClientId = mClientId;
    }

    protected void setStationList(List<Station> mStationList) {
        this.mStationList = mStationList;
    }

    protected void setPlacement(Placement mPlacement) {
        this.mPlacement = mPlacement;
    }

    protected void setStation(Station mStation) {
        this.mStation = mStation;
    }

    protected boolean hasStationList() {
        return this.mStationList != null && this.mStationList.size() > 0;
    }

    protected void setSkippable(boolean skippable) {
        this.mSkippable = skippable;
    }

    protected void setCurrentPlay(Play currentPlay) {
        this.mPlay = currentPlay;

         if ((currentPlay != null) && !playHistory.contains(currentPlay)) {
            while (playHistory.size() >= (maxPlayHistorySize - 1)) {
                playHistory.remove(playHistory.size() - 1);
            }

            playHistory.add(0, currentPlay);
        }
    }

    protected void setState(State state) {
        Log.d(TAG, String.format("PlayInfo.State changed: %s", state.name()));
        this.mState = state;
    }


    // -----------------
    // Public Accessors
    // -----------------

    /**
     * Are the credentials set?
     *
     * @return {@code true} if they are set, {@code false} otherwise.
     */
    public boolean hasCredentials() {
        return mClientId != null;
    }

    /**
     * Version of this library
     *
     * @return The current version of this library. {v#.#}
     */
    public String getSdkVersion() {
        return mSdkVersion;
    }

    /**
     * List of {@link fm.feed.android.playersdk.model.Station}s for the current {@link fm.feed.android.playersdk.model.Placement}
     *
     * @return The list of {@link fm.feed.android.playersdk.model.Station}s for the current {@link fm.feed.android.playersdk.model.Placement}.
     */
    public List<Station> getStationList() {
        return mStationList;
    }

    /**
     * Current {@link fm.feed.android.playersdk.model.Placement} information
     *
     * @return The current {@link fm.feed.android.playersdk.model.Placement} information.
     */
    public Placement getPlacement() {
        return mPlacement;
    }

    /**
     * Currently selected {@link fm.feed.android.playersdk.model.Station}
     *
     * @return The currently selected {@link fm.feed.android.playersdk.model.Station}.
     */
    public Station getStation() {
        return mStation;
    }

    /**
     * Current track ({@link fm.feed.android.playersdk.model.Play}) being played
     *
     * @return The current {@link fm.feed.android.playersdk.model.Play}.
     */
    public Play getPlay() {
        return mPlay;
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
        return mSkippable;
    }

    /**
     * {@link fm.feed.android.playersdk.service.PlayInfo.State} of the Player.
     *
     * @return The current {@link fm.feed.android.playersdk.service.PlayInfo.State}.
     */
    public State getState() {
        return mState;
    }

    /**
     * retrieve play history
     *
     * @return
     */

    public List<Play> getPlayHistory() {
        if (playHistory.size() > maxPlayHistorySize) {
            while (playHistory.size() > maxPlayHistorySize) {
                playHistory.remove(playHistory.size() - 1);
            }
        }

        return playHistory;
    }

    /*
     * This is static so you can set the max size before we've
     * created a PlayInfo object. If you had multiple players
     * this might be non-optimal, but you can't do that in the
     * SDK now anyway.
     */

    public static void setMaxPlayHistorySize(int newDefaultMaxHistorySize) {
        maxPlayHistorySize = newDefaultMaxHistorySize;
    }

}
