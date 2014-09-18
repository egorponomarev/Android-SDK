package fm.feed.android.playersdk.mocks;

import android.media.MediaPlayer;

import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.service.FeedFMMediaPlayer;

import java.io.IOException;

/**
 * Created by mharkins on 8/27/14.
 */
public class DummyMediaPlayer extends FeedFMMediaPlayer {
    @Override
    public State getState() {
        return super.getState();
    }

    @Override
    public Play getPlay() {
        return super.getPlay();
    }

    @Override
    public void setPlay(Play mPlay) {
        super.setPlay(mPlay);
    }

    @Override
    public boolean isAutoPlay() {
        return super.isAutoPlay();
    }

    @Override
    public void setAutoPlay(boolean mAutoPlay) {
        super.setAutoPlay(mAutoPlay);
    }

    @Override
    public void setState(State state) {
        super.setState(state);
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setState(State.INITIALIZED);
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        setState(State.PREPARING);
        onPrepared(this);
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public int getDuration() {
        return 1;
    }

    @Override
    public void start() throws IllegalStateException {
        setState(State.STARTED);
//        onCompletion(this);
    }

    @Override
    public void pause() throws IllegalStateException {
        setState(State.PAUSED);
    }

    @Override
    public void reset() {
        setState(State.IDLE);
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        super.setOnPreparedListener(listener);
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        super.setOnCompletionListener(listener);
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        super.setOnErrorListener(listener);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        super.onCompletion(mp);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return super.onError(mp, what, extra);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        super.onPrepared(mp);
    }
}
