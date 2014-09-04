package fm.feed.android.playersdk.service.bus;

import fm.feed.android.playersdk.model.Play;

/**
 * Created by mharkins on 8/28/14.
 */
public class BufferUpdate {
    private int mPercentage;
    private Play mPlay;

    public BufferUpdate(Play mPlay, int mPercentage) {
        this.mPercentage = mPercentage;
        this.mPlay = mPlay;
    }

    public int getPercentage() {
        return mPercentage;
    }

    public Play getPlay() {
        return mPlay;
    }
}
