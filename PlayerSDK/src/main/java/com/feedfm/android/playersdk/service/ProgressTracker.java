package com.feedfm.android.playersdk.service;

import android.os.Handler;
import android.os.Looper;

import com.feedfm.android.playersdk.model.Play;

import java.util.Date;

/**
 * Created by mharkins on 8/28/14.
 */
public class ProgressTracker {
    private OnProgressListener mOnProgressListener;
    private Handler mUpdateHandler = new Handler(Looper.myLooper());

    private long mStartTimestamp;

    private long mPausedTimestamp;
    private long mOffset;

    private Play mPlay;
    private int mDuration;

    public ProgressTracker(OnProgressListener mOnProgressListener) {
        this.mOnProgressListener = mOnProgressListener;
    }

    public void start(Play play) {
        this.mUpdateHandler.removeCallbacks(this.mUpdateProgress);
        this.mPlay = play;
        this.mDuration = play.getAudioFile().getDurationInSeconds();

        this.mStartTimestamp = new Date().getTime();

        this.mPausedTimestamp = -1;
        this.mOffset = 0;


        this.mUpdateProgress.run();
    }

    public void stop() {
        mUpdateHandler.removeCallbacks(mUpdateProgress);
    }

    public void pause() {
        mPausedTimestamp = new Date().getTime();
    }

    public void resume() {
        // Don't update the offset if the progress was never paused.
        if (mPausedTimestamp < 0) {
            return;
        }

        long now = new Date().getTime();
        mOffset = now - mPausedTimestamp;
    }


    private Runnable mUpdateProgress = new Runnable() {
        @Override
        public void run() {
            long now = new Date().getTime();

            long elapsed = now - (mStartTimestamp - mOffset);

            int elapsedSeconds = (int) (elapsed / 1000);

            mOnProgressListener.onProgressUpdate(mPlay, elapsedSeconds, mDuration);

            if (elapsedSeconds >= mDuration) {
                stop();
            } else {
                mUpdateHandler.postDelayed(mUpdateProgress, 500);
            }
        }

    };

    public interface OnProgressListener {
        public void onProgressUpdate(Play play, int elapsed, int totalDuration);
    }


}
