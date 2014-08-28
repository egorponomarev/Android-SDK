package com.feedfm.android.playersdk.service.bus;

/**
 * Created by mharkins on 8/22/14.
 */
public class EventMessage {
    public enum Status {
        STARTED,
        SKIP_FAILED,
        LIKE,
        UNLIKE,
        DISLIKE
    }

    private Status mStatus;

    public EventMessage(Status status) {
        mStatus = status;
    }

    public Status getStatus() {
        return mStatus;
    }
}
