package com.feedfm.android.playersdk.service.task;

import android.media.MediaPlayer;
import android.util.Log;

import com.feedfm.android.playersdk.model.Play;
import com.feedfm.android.playersdk.service.FeedFMMediaPlayer;
import com.feedfm.android.playersdk.service.TaskQueueManager;
import com.feedfm.android.playersdk.service.webservice.Webservice;

/**
 * Created by mharkins on 9/2/14.
 */
public abstract class PlayTask extends NetworkAbstractTask<Object, Integer, Void> implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    public static final String TAG = PlayTask.class.getSimpleName();

    private FeedFMMediaPlayer mMediaPlayer;
    private Play mPlay;

    private boolean mCompleted = false;
    private boolean mBuffering = true;
    private boolean mSkippable = true;

    private Integer mDuration = 0;

    protected PlayTask(TaskQueueManager queueManager, Webservice mWebservice) {
        super(queueManager, mWebservice);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Log.i(TAG, String.format("%s, onPreExecute", getQueueManager().getIdentifier()));

        FeedFMMediaPlayer mediaPlayer = getMediaPlayer();

        this.mMediaPlayer = mediaPlayer;
        this.mPlay = mediaPlayer.getPlay();

        this.mDuration = this.mMediaPlayer.getDuration();

        this.mMediaPlayer.setOnCompletionListener(this);

        // TODO: might not need this since we have the service running as Foreground
        // this.mMediaPlayer.setWakeMode(this.mService, PowerManager.PARTIAL_WAKE_LOCK);
    }

    @Override
    protected Void doInBackground(Object... params) {
        Log.i(TAG, String.format("%s, doInBackground", getQueueManager().getIdentifier()));

        play();

        onPlayBegin(this, mPlay);

        int i = 0;
        while (!mCompleted && !isSkipped()) {
            if (i % 100 == 0) {
                int bufferUpdatePercentage = this.mMediaPlayer.getLastBufferUpdate();
                boolean doneBuffering = (bufferUpdatePercentage == 100);
                publishProgress(this.mMediaPlayer.getCurrentPosition(), mBuffering ? bufferUpdatePercentage : -1);
                mBuffering = !doneBuffering;
            }
            i++;

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private boolean isSkipped() {
        return isCancelled() && mSkippable;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);

        onProgressUpdate(mPlay, progress[0], mDuration);

        // Once buffering is complete. progress[1] will return null.
        if (progress[1] > 0) {
            onBufferingUpdate(mPlay, progress[1]);
        }
    }

    @Override
    protected void onTaskCancelled() {
        Log.i(TAG, String.format("%s, onCancelled", getQueueManager().getIdentifier()));

        mMediaPlayer.release();
        mMediaPlayer = null;

        onPlayFinished(mPlay, true);
    }

    @Override
    protected void onTaskFinished(Void aVoid) {
        Log.i(TAG, String.format("%s, onPostExecute", getQueueManager().getIdentifier()));

        mMediaPlayer.release();
        mMediaPlayer = null;

        onPlayFinished(mPlay, false);
    }

    public Play getPlay() {
        return mPlay;
    }

    public void setSkippable(boolean skippable) {
        this.mSkippable = skippable;
    }

    public abstract FeedFMMediaPlayer getMediaPlayer();

    public abstract void onPlayBegin(PlayTask playTask, Play play);

    public abstract void onProgressUpdate(Play play, Integer progressInMillis, Integer durationInMillis);

    public abstract void onBufferingUpdate(Play play, Integer percent);

    public abstract void onPlayFinished(Play play, boolean isSkipped);

    public void play() {
        mMediaPlayer.start();
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public boolean isPaused() {
        return mMediaPlayer != null && mMediaPlayer.getState() == FeedFMMediaPlayer.State.PAUSED;
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.getState() == FeedFMMediaPlayer.State.STARTED;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mCompleted = true;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s, play: %s", PlayTask.class.getSimpleName(), getPlay() != null ? getPlay().getAudioFile().getTrack().getTitle() : "(Not Set)");
    }
}
