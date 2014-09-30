package fm.feed.android.playersdk.service.task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.service.FeedFMMediaPlayer;
import fm.feed.android.playersdk.service.constant.Configuration;
import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.util.MediaPlayerPool;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;
import fm.feed.android.playersdk.service.webservice.util.ElapsedTimeManager;

/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * Created by mharkins on 9/2/14.
 */
public class PlayTask extends SkippableTask<Object, Integer, Void> implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener {

    public static final String TAG = PlayTask.class.getSimpleName();

    private Context mContext;
    protected PlayTaskListener mListener;

    private MediaPlayerPool mMediaPlayerPool;

    private WifiManager.WifiLock mWifiLock;
    private ElapsedTimeManager mElapsedTimeManager;

    protected FeedFMMediaPlayer mMediaPlayer;
    protected Play mPlay;

    private boolean mCompleted = false;
    private boolean mBuffering = false;
    private boolean mSkippable = false;
    private int mSystemPauses = 0;
    private boolean mPausedByUser = false;

    private int mLastProgress = 0;

    private Integer mDuration = 0;


    private boolean mPublishProgress = true;
    private Runnable mResetPublishProgressFlag = new Runnable() {
        @Override
        public void run() {
            mPublishProgress = true;
        }
    };
    private Handler mTimingHandler = new Handler(Looper.myLooper());


    private BroadcastReceiver mNoisyAudioBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if (isPlaying()) {
                    // Pause the playback
                    pause(true);
                }
            }
        }
    };

    private BroadcastReceiver mConnectivityBroadcastReceiver = new BroadcastReceiver() {
        private boolean mDidPause = false;
        private boolean mConnected = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            if (!mDidPause && mConnected == isConnected) {
                return;
            }
            mConnected = isConnected;

            Log.d(TAG, "Received Connectivity Broadcast: Connected ? " + isConnected);

            if (!isConnected && isBuffering()) {
                mDidPause = true;

                pause(false);
            } else if (mDidPause) {
                play(false);
            }
        }
    };

    public PlayTask(TaskQueueManager queueManager, Webservice mWebservice, Context context, MediaPlayerPool mediaPlayerPool, ElapsedTimeManager elapsedTimeManager, PlayTaskListener listener) {
        super(queueManager, mWebservice);

        this.mContext = context;
        this.mListener = listener;
        this.mMediaPlayerPool = mediaPlayerPool;
        this.mElapsedTimeManager = elapsedTimeManager;

        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mWifiLock = wifiManager.createWifiLock(Configuration.WIFI_LOCK_TAG);
        mWifiLock.setReferenceCounted(false);

        // Register Noisy Audio Receiver.
        // When audio becomes noisy (speaker jack is removed), we want to cut off the noise level.
        // Refer to http://developer.android.com/training/managing-audio/audio-output.html#HandleChanges for details.
        mContext.registerReceiver(mNoisyAudioBroadcastReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

        mContext.registerReceiver(mConnectivityBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public Play getPlay() {
        return mPlay;
    }

    public void setSkippable(boolean skippable) {
        this.mSkippable = skippable;
    }

    public boolean isSkippableCandidate() {
        return mSkippable;
    }

    public boolean isBuffering() {
        return mBuffering;
    }

    @Override
    public Integer getElapsedTimeMillis() {
        return mLastProgress;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Log.i(TAG, String.format("%s, onPreExecute", getQueueManager().getIdentifier()));

        FeedFMMediaPlayer mediaPlayer = mMediaPlayerPool.getTunedMediaPlayer();
        if (mediaPlayer == null) {
            Log.e(TAG, String.format("%s, Media Player is Null", getQueueManager().getIdentifier()));
            cancel(true);
            return;
        }

        mMediaPlayer = mediaPlayer;
        mPlay = mediaPlayer.getPlay();

        mDuration = mMediaPlayer.getDuration();

        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);

        mWifiLock.acquire();
    }

    @Override
    protected Void doInBackground(Object... params) {
        Log.i(TAG, String.format("%s, doInBackground", getQueueManager().getIdentifier()));

        mLastProgress = 0;
        mBuffering = true;
        mMediaPlayer.setLooping(true);

        // Start Play.
        publishPlay();

        // Publish Start of Play.
        publishPlayBegin();

        while (!mCompleted && !isCancelled()) {
            int currentProgress = mMediaPlayer.getCurrentPosition();
            boolean done = mDuration == currentProgress;

            /**
             * If the last recorded progress is greater than the current one, it means that the play has looped.
             * In that case, we should seek back to the mLastProgress and pause playback
             */
            boolean skippedBack = mLastProgress > currentProgress;

            if (skippedBack || done) {
                // If we are already done buffering, then the play is done, and we should just end this task.
                mMediaPlayer.setLooping(false);
                mMediaPlayer.stop();
                break;
            }

            if (mPublishProgress) {
                mPublishProgress = false;
                mTimingHandler.postDelayed(mResetPublishProgressFlag, Configuration.PROGRESS_PUBLISH_INTERVAL);

                publishProgress();
            }

            try {
                // Make the tread sleep for 5 milliseconds
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    protected void publishPlay() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                play(true);
            }
        });
    }

    protected void publishPlayBegin() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onPlayBegin(PlayTask.this, mPlay);
                }
            }
        });
    }

    protected void publishProgress() {
        int currentProgress = mMediaPlayer.getCurrentPosition();

        // Publish progress every 5
        int bufferUpdatePercentage = mMediaPlayer.getLastBufferUpdate();
        boolean doneBuffering = (bufferUpdatePercentage == 100);

        // Publish the progress only if we are playing.
        publishProgress(currentProgress, isBuffering() ? bufferUpdatePercentage : -1);

        mLastProgress = currentProgress;

        mBuffering = !doneBuffering;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);

        if (mListener != null) {
            mListener.onProgressUpdate(mPlay, progress[0], mDuration);

            // Once buffering is complete. progress[1] will return null.
            if (progress[1] >= 0) {
                mListener.onBufferingUpdate(mPlay, progress[1]);
            }
        }
    }

    @Override
    protected void onTaskCancelled(FeedFMError error, int attempt) {
        Log.d(TAG, "TASK CANCELLED: " + toString());
        if (error == null || (attempt >= Configuration.MAX_TASK_RETRY_ATTEMPTS)) {

            if (mListener != null) {
                mListener.onPlayFinished(mPlay, true);
            }

        } else {
            getQueueManager().offerFirst(copy(attempt));
        }

        cleanup();
    }

    @Override
    protected void onTaskFinished(Void aVoid) {
        Log.i(TAG, String.format("%s, onPostExecute", getQueueManager().getIdentifier()));

        if (mListener != null) {
            mListener.onPlayFinished(mPlay, false);
        }

        cleanup();
    }

    @Override
    public PlayerAbstractTask copy(int attempts) {
        PlayerAbstractTask task = new PlayTask(getQueueManager(), mWebservice, mContext, mMediaPlayerPool, mElapsedTimeManager, mListener);
        task.setAttemptCount(attempts);
        return task;
    }

    private void cleanup() {
        mWifiLock.release();
        mElapsedTimeManager.stop();
        mTimingHandler.removeCallbacks(mResetPublishProgressFlag);
        mContext.unregisterReceiver(mNoisyAudioBroadcastReceiver);
        mContext.unregisterReceiver(mConnectivityBroadcastReceiver);

        if (mMediaPlayer != null) {
            mMediaPlayerPool.free(mMediaPlayer);
            mMediaPlayer = null;
        }
    }

    public void play(boolean isUserInteraction) {
        if (isUserInteraction) {
            mPausedByUser = false;
        } else {
            mSystemPauses--;
        }

        // Only resume when User has requested it and when the System is done with the Pauses.
        if (mPausedByUser || mSystemPauses > 0) {
            return;
        }

        mWifiLock.acquire();
        mMediaPlayer.start();
        mElapsedTimeManager.start(this);

        if (mListener != null) {
            mListener.onPlay(this);
        }
    }

    public void pause(boolean isUserInteraction) {
        if (isUserInteraction) {
            mPausedByUser = true;
        }

        // Pauses my be requested by multiple systems
        // ie. Audio Focus + Network
        if (!isUserInteraction) {
            mSystemPauses++;
        }

        mWifiLock.release();
        mMediaPlayer.pause();
        mElapsedTimeManager.stop();

        if (mListener != null) {
            mListener.onPause(this);
        }
    }

    public void setVolume(float leftVolume, float rightVolume) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(leftVolume, rightVolume);
        }
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
    public String getTag() {
        return PlayTask.class.getSimpleName();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mListener != null) {
                    mListener.onBufferingStarted(this);
                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (mListener != null) {
                    mListener.onBufferingEnded(this);
                }
        }
        return true;
    }

    public interface PlayTaskListener {
        public void onPlayBegin(PlayTask playTask, Play play);

        public void onPlay(PlayTask playTask);

        public void onPause(PlayTask playTask);

        public void onBufferingStarted(PlayTask playTask);

        public void onBufferingEnded(PlayTask playTask);

        public void onProgressUpdate(Play play, Integer progressInMillis, Integer durationInMillis);

        public void onBufferingUpdate(Play play, Integer percent);

        public void onPlayFinished(Play play, boolean isSkipped);
    }

    @Override
    public String toString() {
        return String.format("%s, play: %s", PlayTask.class.getSimpleName(), getPlay() != null ? getPlay().getAudioFile().getTrack().getTitle() : "(Not Set)");
    }

}
