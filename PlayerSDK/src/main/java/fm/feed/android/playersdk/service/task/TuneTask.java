package fm.feed.android.playersdk.service.task;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.service.FeedFMMediaPlayer;
import fm.feed.android.playersdk.util.MediaPlayerPool;
import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;
import fm.feed.android.playersdk.service.webservice.model.PlayerInfo;

/**
 * Created by mharkins on 9/2/14.
 */
public class TuneTask extends MediaPlayerAbstractTask<Object, Integer, FeedFMMediaPlayer> implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {
    private static final String TAG = TuneTask.class.getSimpleName();

    public static final int MAX_RETRY_ATTEMPTS = 2;

    public interface TuneTaskListener {
        public void onMetaDataLoaded(TuneTask tuneTask, Play play);

        public void onSuccess(TuneTask tuneTask, FeedFMMediaPlayer mediaPlayer, Play play);

        public void onApiError(FeedFMError mApiError);
    }

    private TuneTaskListener mListener;

    private MediaPlayerPool mMediaPlayerPool;
    private PlayerInfo mPlayerInfo;

    private FeedFMMediaPlayer mMediaPlayer;

    private boolean mPrepared = false;
    private int mRetryAttempts = 0;
    private boolean mShouldRetry = false;

    private FeedFMError mApiError = null;


    public TuneTask(TaskQueueManager queueManager, Webservice mWebservice, MediaPlayerPool mediaPlayerPool, TuneTaskListener listener, PlayerInfo playerInfo) {
        super(queueManager, mWebservice);

        this.mMediaPlayerPool = mediaPlayerPool;
        this.mPlayerInfo = playerInfo;
        this.mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected FeedFMMediaPlayer doInBackground(Object... params) {
        Log.i(TAG, String.format("%s, doInBackground", getQueueManager().getIdentifier()));

        // This exception is called if the MediaPlayer dataSource is set while the media player is in an invalid state.
        try {
            Play play = mWebservice.getPlay(mPlayerInfo.getClientId(), mPlayerInfo.getPlacement(), mPlayerInfo.getStation(),
                    null, // For now don't put in the AudioFormat
                    null);

            if (this.mListener != null) {
                this.mListener.onMetaDataLoaded(this, play);
            }

            prepare(play, 0);
        } catch (FeedFMError feedFMError) {
            mApiError = feedFMError;
            return null;
        } catch (IOException e) {
            // TODO-XX handle otherwise.
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            // TODO: handle fail
        }

        return mMediaPlayer;
    }

    protected void prepare(Play play, int attempts) throws IOException, IllegalStateException {
        if (attempts > MAX_RETRY_ATTEMPTS) {
            cancel(true);
            Log.e(TAG, "Could not retry preparing of MediaPlayer");
            return;
        }

        mMediaPlayer = mMediaPlayerPool.getTuningMediaPlayer();
        mMediaPlayer.setOnErrorListener(this);

        mMediaPlayer.setState(FeedFMMediaPlayer.State.FETCHING_METADATA);

        mMediaPlayer.setPlay(play);
        mMediaPlayer.setOnPreparedListener(this);

        Log.i(TAG, String.format(">>>>>>>>>>>>>>>MediaPlayer.preparing (%d): %s....", attempts, play.getAudioFile().getTrack().getTitle()));

        mMediaPlayer.setDataSource(play.getAudioFile().getUrl());
        mMediaPlayer.prepareAsync();

        while (!mPrepared && !isCancelled() && !mShouldRetry) {
            try {
                // Make the tread sleep for 5 milliseconds
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // If the MediaPlayer fails to prepare the track, we have it retry.
        if (mShouldRetry) {
            mShouldRetry = false;

            prepare(play, ++attempts);
        }
        Log.i(TAG, String.format(">>>>>>>>>>>>>>>>>>>...MediaPlayer.preparing: %s", play.getAudioFile().getTrack().getTitle()));
    }

    @Override
    protected void onTaskFinished(FeedFMMediaPlayer feedFMMediaPlayer) {
        Log.i(TAG, String.format("%s, onTaskFinished", getQueueManager()));

        if (feedFMMediaPlayer != null) {
            feedFMMediaPlayer.setOnErrorListener(null);
            mMediaPlayerPool.putTunedMediaPlayer(feedFMMediaPlayer);

            if (this.mListener != null) {
                this.mListener.onSuccess(this, feedFMMediaPlayer, feedFMMediaPlayer.getPlay());
            }
        } else if (mApiError != null) {
            if (this.mListener != null) {
                this.mListener.onApiError(mApiError);
            }
        }
    }

    @Override
    protected void onTaskCancelled() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnErrorListener(null);
            mMediaPlayerPool.free(mMediaPlayer);
            mMediaPlayer = null;
        }
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
            mMediaPlayerPool.free(mediaPlayer);
            mMediaPlayer = null;
            mShouldRetry = true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s", TuneTask.class.getSimpleName());
    }
}
