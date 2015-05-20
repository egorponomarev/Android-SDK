package fm.feed.android.playersdk;

import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.Station;

/**
 * Implement this interface to get callbacks from the Player
 */
public interface NavListener {

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
     *         The current {@link Play}
     * @param percentage
     *         The percentage buffered
     */
    public void onBufferUpdate(Play play, int percentage);

    /**
     * Called repeatedly while the Audio is playing
     *
     * @param play
     *         The current {@link Play}
     * @param elapsedTime
     *         How far along in the track the current {@link Play} is.
     * @param totalTime
     *         The duration of the track.
     */
    public void onProgressUpdate(Play play, int elapsedTime, int totalTime);
}
