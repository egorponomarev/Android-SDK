package fm.feed.android.playersdk;

import fm.feed.android.playersdk.service.PlayInfo;

/**
 * Implement this interface to get callbacks from the Player
 */

public interface PlayerListener {

    /**
     * Called when the {@link Player} is initialized and authenticated with the server and ready to play music
     *
     * @param playInfo
     *         The {@link fm.feed.android.playersdk.service.PlayInfo} object containing the play state as well as other information pertaining to the library.
     */
    public void onPlayerInitialized(PlayInfo playInfo);

    /**
     * Called when the playback state changes
     * <p>
     * Can be one of:
     * <ul>
     * <li>{@link PlayInfo.State#WAITING} - Player is Waiting for Metadata from the Server</li>
     * <li>{@link PlayInfo.State#READY} - Player is ready to play music</li>
     * <li>{@link PlayInfo.State#PAUSED} - Audio playback is currently paused</li>
     * <li>{@link PlayInfo.State#PLAYING} - Audio playback is currently processing</li>
     * <li>{@link PlayInfo.State#STALLED} - Audio Playback has paused due to lack of audio data from the server</li>
     * <li>{@link PlayInfo.State#COMPLETE} - The player has run out of available music to play</li>
     * <li>{@link PlayInfo.State#REQUESTING_SKIP} - The player is waiting for the server to say if the current song can be skipped</li>
     * </ul>
     * </p>
     *
     * @param state
     *         {@link PlayInfo.State} of the Player.
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
