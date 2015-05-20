package fm.feed.android.playersdk;

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
