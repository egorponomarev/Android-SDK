package fm.feed.android.playersdk;

/**
 * See Player.onPlayerAvailability() for how this interface is used.
 *
 */

public interface PlayerAvailabilityListener {

    /*
     * Called at most one time, when we know the player is initialized and
     * able to play music.
     */

    public void onAvailable();

    /*
     * Called at most one time, when we know the player is unable to play
     * music due to licensing restrictions or an inability to contact the
     * Feed.fm servers.
     */

    public void onUnavailable();

}
