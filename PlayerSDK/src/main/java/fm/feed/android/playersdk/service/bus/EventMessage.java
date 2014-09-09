package fm.feed.android.playersdk.service.bus;

/**
 * Created by mharkins on 8/22/14.
 */
public class EventMessage {
    public enum Status {
        SKIP_FAILED,
        LIKE,
        UNLIKE,
        DISLIKE,
        END_OF_PLAYLIST,
        NOT_IN_US,
        NOTIFICATION_WILL_SHOW,
        STATUS_UPDATED
    }

    private Status mStatus;

    public EventMessage(Status status) {
        mStatus = status;
    }

    public Status getStatus() {
        return mStatus;
    }
}
