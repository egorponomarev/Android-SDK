package fm.feed.android.playersdk.service;

import android.media.MediaPlayer;
import android.util.Log;

import fm.feed.android.playersdk.model.Play;

import java.io.IOException;

/**
 * Created by mharkins on 8/25/14.
 */
public class FeedFMMediaPlayer extends MediaPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener {
    private static final String TAG = FeedFMMediaPlayer.class.getSimpleName();

    private State mState = State.IDLE;
    private State mPrevState;

    private Play mPlay;
    private boolean mAutoPlay;

    private boolean mSkipped = false;
    private int mLastBufferUpdate;

    private OnPreparedListener mOnPreparedListener;
    private OnCompletionListener mOnCompletionListener;
    private OnErrorListener mOnErrorListener;

    public static enum State {
        IDLE,
        FETCHING_METADATA,
        INITIALIZED,
        PREPARING,
        PREPARED,
        STARTED,
        STOPPED,
        PAUSED,
        COMPLETE,
        END,
        ERROR
    }

    public FeedFMMediaPlayer() {
        setOnBufferingUpdateListener(this);
    }

    public State getState() {
        return mState;
    }

    public Play getPlay() {
        return mPlay;
    }

    public void setPlay(Play mPlay) {
        this.mPlay = mPlay;
    }

    public boolean isAutoPlay() {
        return mAutoPlay;
    }

    public void setAutoPlay(boolean mAutoPlay) {
        this.mAutoPlay = mAutoPlay;
    }

    public void setState(State state) {
        this.mState = state;
    }

    public State getPrevState() {
        return mPrevState;
    }

    public boolean isSkipped() {
        return mSkipped;
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        synchronized (this) {
            Log.d(TAG, String.format("setting DataSource to %s. Current State: %s", path, getState().name()));

            super.setDataSource(path);
            mState = State.INITIALIZED;
        }
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        mLastBufferUpdate = 0;
        synchronized (this) {
            super.prepareAsync();
            mState = State.PREPARING;
        }
    }

    @Override
    public void start() throws IllegalStateException {
        synchronized (this) {
            super.start();
            mState = State.STARTED;
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        synchronized (this) {
            super.pause();
            mState = State.PAUSED;
        }
    }

    public void silentReset() {
        mSkipped = true;
        reset();
        mSkipped = false;
    }

    @Override
    public void reset() {
        synchronized (this) {
            super.reset();
            mState = State.IDLE;
        }
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        super.setOnPreparedListener(this);

        mOnPreparedListener = listener;
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        super.setOnCompletionListener(this);

        mOnCompletionListener = listener;
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        super.setOnErrorListener(this);

        mOnErrorListener = listener;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (!mSkipped) {
            synchronized (mState) {
                mState = State.COMPLETE;
            }
            mOnCompletionListener.onCompletion(mp);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        synchronized (mState) {
            mPrevState = mState;
            mState = State.ERROR;
        }
        boolean retval = false;
        if (mOnErrorListener != null) {
            retval = mOnErrorListener.onError(mp, what, extra);
        } else {
            Log.e(TAG, String.format("error playing track: [%s]: (%d, %d)", this.getPlay().getAudioFile().getTrack().getTitle(), what, extra));
        }
        return retval;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        synchronized (mState) {
            mState = State.PREPARED;
        }
        mOnPreparedListener.onPrepared(mp);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        mLastBufferUpdate = percent;
    }

    public int getLastBufferUpdate() {
        return mLastBufferUpdate;
    }
}