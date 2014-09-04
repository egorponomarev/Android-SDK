package fm.feed.android.playersdk.service.bus;

import fm.feed.android.playersdk.model.Play;

/**
 * Created by mharkins on 8/28/14.
 */
public class ProgressUpdate {
    private int mElapsedTime;
    private int mTotalTime;

    private Play mPlay;

    public ProgressUpdate(Play mPlay, int elapsedTime, int totalTime) {
        this.mElapsedTime = elapsedTime;
        this.mTotalTime = totalTime;
        this.mPlay = mPlay;
    }

    public Play getPlay() {
        return mPlay;
    }

    public int getElapsedTime() {
        return mElapsedTime;
    }

    public int getTotalTime() {
        return mTotalTime;
    }
}
