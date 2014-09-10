package fm.feed.android.playersdk.service.task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.service.FeedFMMediaPlayer;
import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.util.MediaPlayerPool;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
 * Created by mharkins on 9/2/14.
 */
public class PlayTask extends NetworkAbstractTask<Object, Integer, Void> implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener {
    public static final String TAG = PlayTask.class.getSimpleName();

    public static final int PROGRESS_PUBLISH_INTERVAL = 500; // 0.5 seconds

    private MediaPlayerPool mMediaPlayerPool;

    private FeedFMMediaPlayer mMediaPlayer;
    private Play mPlay;

    private boolean mCompleted = false;
    private boolean mBuffering = true;
    private boolean mSkippable = true;

    private Integer mDuration = 0;

    private PlayTaskListener mListener;

    private boolean mPublishProgress = true;
    private Runnable mResetPublishProgressFlag = new Runnable() {
        @Override
        public void run() {
            mPublishProgress = true;
        }
    };
    private Handler mTimingHandler = new Handler(Looper.myLooper());

    private Context mContext;
    private BroadcastReceiver mNoisyAudioBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if (isPlaying()) {
                    // Pause the playback
                    pause();
                }
            }
        }
    };

    public PlayTask(TaskQueueManager queueManager, Webservice mWebservice, Context context, MediaPlayerPool mediaPlayerPool, PlayTaskListener listener) {
        super(queueManager, mWebservice);

        this.mContext = context;
        mListener = listener;
        this.mMediaPlayerPool = mediaPlayerPool;

        // Register Noisy Audio Receiver.
        // When audio becomes noisy (speaker jack is removed), we want to cut off the noise level.
        // Refer to http://developer.android.com/training/managing-audio/audio-output.html#HandleChanges for details.
        mContext.registerReceiver(mNoisyAudioBroadcastReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
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
    }

    @Override
    protected Void doInBackground(Object... params) {
        Log.i(TAG, String.format("%s, doInBackground", getQueueManager().getIdentifier()));

        play();

        if (mListener != null) {
            mListener.onPlayBegin(this, mPlay);
        }

        while (!mCompleted && !isCancelled()) {
            if (mPublishProgress) {
                mPublishProgress = false;
                mTimingHandler.postDelayed(mResetPublishProgressFlag, PROGRESS_PUBLISH_INTERVAL);

                // Publish progress every 5
                int bufferUpdatePercentage = mMediaPlayer.getLastBufferUpdate();
                boolean doneBuffering = (bufferUpdatePercentage == 100);
                publishProgress(mMediaPlayer.getCurrentPosition(), isBuffering() ? bufferUpdatePercentage : -1);
                mBuffering = !doneBuffering;
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

    public boolean isSkippable() {
        return mSkippable;
    }

    public boolean isBuffering() {
        return mBuffering;
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
        if (error == null || (attempt >= MAX_TASK_RETRY_ATTEMPTS)) {

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
        PlayerAbstractTask task = new PlayTask(getQueueManager(), mWebservice, mContext, mMediaPlayerPool, mListener);
        task.setAttemptCount(attempts);
        return task;
    }

    private void cleanup() {
        mTimingHandler.removeCallbacks(mResetPublishProgressFlag);

        if (mMediaPlayer != null) {
            mMediaPlayerPool.free(mMediaPlayer);
            mMediaPlayer = null;
        }
    }

    public Play getPlay() {
        return mPlay;
    }

    public void setSkippable(boolean skippable) {
        this.mSkippable = skippable;
    }

    public void play() {
        mMediaPlayer.start();
        mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);

        if (mListener != null) {
            mListener.onPlay(this);
        }
    }

    public void pause() {
        mMediaPlayer.pause();

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
        Log.d(TAG, String.format("onCompletion was called: (pos: %d, dur: %d", mp.getCurrentPosition(), mp.getDuration()));
        mCompleted = true;
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
