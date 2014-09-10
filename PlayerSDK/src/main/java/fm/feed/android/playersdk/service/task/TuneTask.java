package fm.feed.android.playersdk.service.task;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.service.FeedFMMediaPlayer;
import fm.feed.android.playersdk.service.PlayInfo;
import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.util.MediaPlayerPool;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
 * Created by mharkins on 9/2/14.
 */
public class TuneTask extends MediaPlayerAbstractTask<Object, Integer, FeedFMMediaPlayer> implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener {
    private static final String TAG = TuneTask.class.getSimpleName();

    public static final int MAX_MEDIA_ERROR_RETRY_ATTEMPTS = 2;

    public interface TuneTaskListener {
        public void onMetaDataLoaded(TuneTask tuneTask, Play play);

        public void onBufferingStarted();

        public void onBufferingEnded();

        public void onSuccess(TuneTask tuneTask, FeedFMMediaPlayer mediaPlayer, Play play);

        public void onApiError(FeedFMError mApiError);
    }

    private TuneTaskListener mListener;

    private MediaPlayerPool mMediaPlayerPool;
    private PlayInfo mPlayInfo;

    private String mClientId;

    private FeedFMMediaPlayer mMediaPlayer;

    private boolean mPrepared = false;

    private Play mPlay;


    public TuneTask(TaskQueueManager queueManager, Webservice mWebservice, MediaPlayerPool mediaPlayerPool, TuneTaskListener listener, PlayInfo playInfo, String clientId) {
        this(queueManager, mWebservice, mediaPlayerPool, listener, playInfo, null, clientId);
    }

    public TuneTask(TaskQueueManager queueManager, Webservice mWebservice, MediaPlayerPool mediaPlayerPool, TuneTaskListener listener, PlayInfo playInfo, Play play, String clientId) {
        super(queueManager, mWebservice);

        this.mPlay = null;
        this.mMediaPlayerPool = mediaPlayerPool;
        this.mPlayInfo = playInfo;
        this.mClientId = clientId;
        this.mListener = listener;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected FeedFMMediaPlayer doInBackground(Object... params) {
        Log.i(TAG, String.format("%s, doInBackground", getQueueManager().getIdentifier()));

        try {
            // When retrying this task, the Play information might already have been collected.
            if (mPlay == null) {
                mPlay = mWebservice.getPlay(mClientId, mPlayInfo.getPlacement(), mPlayInfo.getStation(),
                        null, // For now don't put in the AudioFormat
                        null);
            }

            if (mListener != null) {
                mListener.onMetaDataLoaded(this, mPlay);
            }

            mMediaPlayer = mMediaPlayerPool.getTuningMediaPlayer();
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnInfoListener(this);

            mMediaPlayer.setState(FeedFMMediaPlayer.State.FETCHING_METADATA);

            mMediaPlayer.setPlay(mPlay);
            mMediaPlayer.setOnPreparedListener(this);

            Log.i(TAG, String.format(">>>>>>>>>>>>>>>MediaPlayer.preparing (%d): %s....", getAttemptCount(), mPlay.getAudioFile().getTrack().getTitle()));

            mMediaPlayer.setDataSource(mPlay.getAudioFile().getUrl());

            mMediaPlayer.prepareAsync();

            while (!mPrepared && !isCancelled()) {
                try {
                    // Make the tread sleep for 5 milliseconds
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (FeedFMError feedFMError) {
            cancel(feedFMError);
        } catch (IOException e) {
            e.printStackTrace();
            cancel(new FeedFMError(-1, "Error Tuning MediaPlayer (IOException)", -1));
        } catch (IllegalStateException e) {
            // This exception is called if the MediaPlayer dataSource is set while the media player is in an invalid state.
            e.printStackTrace();
            cancel(new FeedFMError(-1, "Error Tuning MediaPlayer (IllegalStateException)", -1));
        }

        return mMediaPlayer;
    }

    protected void prepare(Play play, int attempts) throws IOException, IllegalStateException {
        if (attempts > MAX_MEDIA_ERROR_RETRY_ATTEMPTS) {
            cancel(true);
            Log.e(TAG, "Could not retry preparing of MediaPlayer");
            return;
        }
    }

    @Override
    protected void onTaskFinished(FeedFMMediaPlayer feedFMMediaPlayer) {
        Log.i(TAG, String.format(">>>>>>>>>>>>>>>>>>>...MediaPlayer.preparing: %s", mPlay.getAudioFile().getTrack().getTitle()));

        Log.i(TAG, String.format("%s, onTaskFinished", getQueueManager()));

        if (feedFMMediaPlayer != null) {
            feedFMMediaPlayer.setOnErrorListener(null);
            mMediaPlayerPool.putTunedMediaPlayer(feedFMMediaPlayer);

            if (this.mListener != null) {
                this.mListener.onSuccess(this, feedFMMediaPlayer, feedFMMediaPlayer.getPlay());
            }
        }
    }

    @Override
    protected void onTaskCancelled(FeedFMError error, int attempt) {
        if (error != null) {
            if (attempt < MAX_TASK_RETRY_ATTEMPTS) {
                getQueueManager().offerFirst(copy(attempt + 1));
            } else if (this.mListener != null) {
                this.mListener.onApiError(error);
            }
        }

        if (mMediaPlayer != null) {
            mMediaPlayer.setOnErrorListener(null);
            mMediaPlayerPool.release(mMediaPlayer);
            mMediaPlayer = null;
        }
    }

    @Override
    public PlayerAbstractTask copy(int attempts) {
        PlayerAbstractTask task = new TuneTask(getQueueManager(), mWebservice, mMediaPlayerPool, mListener, mPlayInfo, mPlay, mClientId);
        task.setAttemptCount(attempts);
        return task;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPrepared = true;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        FeedFMMediaPlayer mediaPlayer = (FeedFMMediaPlayer) mp;
        Log.e(TAG, String.format("error playing track: [%s]: (%d, %d)", mediaPlayer.getPlay().getAudioFile().getTrack().getTitle(), what, extra));

        if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            FeedFMError error = new FeedFMError(-1, "Unknown Media Error from MediaPlayer.", -1);
            Log.e(TAG, error.toString());

            if (getAttemptCount() < MAX_MEDIA_ERROR_RETRY_ATTEMPTS) {
                cancel(error);
            }
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mListener != null) {
                    mListener.onBufferingStarted();
                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (mListener != null) {
                    mListener.onBufferingEnded();
                }
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("{%s - isCancelled: %s}", TuneTask.class.getSimpleName(), isCancelled());
    }
}
