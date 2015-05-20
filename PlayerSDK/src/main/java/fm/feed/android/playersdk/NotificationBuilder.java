package fm.feed.android.playersdk;

import android.app.Notification;
import android.content.Context;

import fm.feed.android.playersdk.model.Play;

/**
 * Music can continue to play while our containing app is 'background'ed so, by
 * default, we create a Notification that shows the currently playing song and
 * gives the user a way to return back to the app to interact with the player.
 * An instance of this class is used by the player to generate that Notification
 * and update it as songs change.
 *
 * To effectively disable notifications, have the 'build' method return NULL.
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

    /*
     * This should be a non-changing unique number that identifies the notification and lets us remove
     * the notification when it needs to be updated.
     */

    public int getNotificationId();
}
