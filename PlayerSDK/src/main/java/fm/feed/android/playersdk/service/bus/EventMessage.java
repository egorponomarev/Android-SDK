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
        NOT_IN_US
    }

    private Status mStatus;

    public EventMessage(Status status) {
        mStatus = status;
    }

    public Status getStatus() {
        return mStatus;
    }
}
