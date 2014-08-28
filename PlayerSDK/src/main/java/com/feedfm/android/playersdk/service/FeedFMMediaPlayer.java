package com.feedfm.android.playersdk.service;

import android.media.MediaPlayer;

import com.feedfm.android.playersdk.model.Play;

import java.io.IOException;

/**
 * Created by mharkins on 8/25/14.
 */
public class FeedFMMediaPlayer extends MediaPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private State mState = State.IDLE;
    private Play mPlay;
    private boolean mAutoPlay;

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

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        synchronized (this) {
            super.setDataSource(path);
            mState = State.INITIALIZED;
        }
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
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
        synchronized (mState) {
            mState = State.COMPLETE;
        }
        mOnCompletionListener.onCompletion(mp);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        synchronized (mState) {
            mState = State.ERROR;
        }
        return mOnErrorListener.onError(mp, what, extra);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        synchronized (mState) {
            mState = State.PREPARED;
        }
        mOnPreparedListener.onPrepared(mp);
    }
}
