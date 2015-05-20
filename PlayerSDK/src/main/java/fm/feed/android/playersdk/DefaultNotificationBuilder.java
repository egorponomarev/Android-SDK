package fm.feed.android.playersdk;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;

import fm.feed.android.playersdk.model.Play;

/**
 * This class is the default used by the Player to create a Notification that tells the user
 * about the song currently being played.
 *
 */

public class DefaultNotificationBuilder implements NotificationBuilder {

    public static final int DEFAULT_FEED_NOTIFICATION_ID = 121231337;

    /*
     * Return a Notification that says 'Playing: {song info}' and will launch the
     * current activity when tapped.
     */

    @Override
    public Notification build(Context serviceContext, Play play) {
        String title = play.getAudioFile().getTrack().getTitle();

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(serviceContext);
        mBuilder.setContentIntent(getPendingIntent(serviceContext));
        mBuilder.setContentTitle(getApplicationName(serviceContext));
        mBuilder.setContentText(serviceContext.getString(R.string.notification_body_template, title));
        mBuilder.setOngoing(true);
        mBuilder.setSmallIcon(android.R.drawable.ic_media_play);

        // NOTIFICATION_ID allows you to update the notification later on.
        return mBuilder.build();
    }

    /*
     * Return a unique number for the player notification.
     */

    @Override
    public int getNotificationId() {
        return DEFAULT_FEED_NOTIFICATION_ID;
    }

    private String cachedApplicationName;
    private PendingIntent cachedPendingIntent;

    private String getApplicationName(Context serviceContext) {
        if (cachedApplicationName == null) {
            int stringId = serviceContext.getApplicationInfo().labelRes;
            cachedApplicationName = serviceContext.getString(stringId);
        }

        return cachedApplicationName;
    }

    private PendingIntent getPendingIntent(Context serviceContext) {
        if (cachedPendingIntent == null) {
            Intent i = null;
            PackageManager manager = serviceContext.getPackageManager();
            try {
                i = manager.getLaunchIntentForPackage(serviceContext.getApplicationInfo().packageName);
                if (i == null)
                    throw new PackageManager.NameNotFoundException();

                i.addCategory(Intent.CATEGORY_LAUNCHER);
                i.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                cachedPendingIntent = PendingIntent.getActivity(serviceContext.getApplicationContext(), 0, i,
                        PendingIntent.FLAG_UPDATE_CURRENT);

            } catch (PackageManager.NameNotFoundException e) {
                cachedPendingIntent = null;
            }
        }

        return cachedPendingIntent;
    }

}
