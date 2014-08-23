package com.feedfm.android.playersdk.service.bus;

/**
 * Created by mharkins on 8/22/14.
 */
public class StatusMessage {
    public enum Status {
        STARTED
    }

    private Status mStatus;

    public StatusMessage(Status status) {
        mStatus = status;
    }

    public Status getStatus() {
        return mStatus;
    }
}
