package fm.feed.android.playersdk.service.constant;

import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
 * Created by mharkins on 9/11/14.
 */
public class Configuration {
    /** Maximum number of retry attempts for failed tasks (usually when there's a connectivity problem). */
    public static final int MAX_TASK_RETRY_ATTEMPTS = 1;
    /** Publish progress should only be sent out to the front of the application every X seconds. */
    public static final int PROGRESS_PUBLISH_INTERVAL = 500; // 0.5 seconds

    /** Elapsed time of the song is published every 10 seconds */
    public static final int ELAPSED_PING_INTERVAL = 10000; // 10 seconds

    /**
     * Maximum number of Force Skips in a row.
     * This would happen when an audio file can't be parsed properly by the Media Player.
     */
    public static final int MAX_FORCE_SKIP_COUNT = 2;


    public static final String WIFI_LOCK_TAG = "fm.feed.wifilock";
}
