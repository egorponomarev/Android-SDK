package fm.feed.android.playersdk.service.task;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;

import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.service.FeedFMMediaPlayer;
import fm.feed.android.playersdk.service.PlayInfo;
import fm.feed.android.playersdk.service.constant.Configuration;
import fm.feed.android.playersdk.service.constant.PlayerErrorEnum;
import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.util.MediaPlayerPool;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.model.FeedFMConnectivityError;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Created by mharkins on 9/2/14.
 */
public class TuneTask extends SkippableTask<Object, Integer, FeedFMMediaPlayer> implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener {

    private static final String TAG = TuneTask.class.getSimpleName();

    public interface TuneTaskListener {
        public void onTuneTaskBegin(TuneTask tuneTask);

        public void onMetaDataLoaded(TuneTask tuneTask, Play play);

        public void onBufferingStarted();

        public void onBufferingEnded();

        public void onSuccess(TuneTask tuneTask, FeedFMMediaPlayer mediaPlayer, Play play);

        public void onApiError(TuneTask tuneTask, FeedFMError apiError);

        public void onUnkownError(TuneTask tuneTask, FeedFMError feedFMError);
    }

    private Context mContext;

    private TuneTaskListener mListener;

    private MediaPlayerPool mMediaPlayerPool;
    private PlayInfo mPlayInfo;

    private String mClientId;

    private FeedFMMediaPlayer mMediaPlayer;

    private boolean mPrepared = false;

    private Play mPlay;

    public TuneTask(Context context, TaskQueueManager queueManager, Webservice mWebservice, MediaPlayerPool mediaPlayerPool, TuneTaskListener listener, PlayInfo playInfo, String clientId) {
        this(context, queueManager, mWebservice, mediaPlayerPool, listener, playInfo, null, clientId);
    }

    public TuneTask(Context context, TaskQueueManager queueManager, Webservice mWebservice, MediaPlayerPool mediaPlayerPool, TuneTaskListener listener, PlayInfo playInfo, Play play, String clientId) {
        super(queueManager, mWebservice);

        this.mContext = context;
        this.mPlay = null;
        this.mMediaPlayerPool = mediaPlayerPool;
        this.mPlayInfo = playInfo;
        this.mClientId = clientId;
        this.mListener = listener;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (mListener != null) {
            mListener.onTuneTaskBegin(this);
        }
    }

    @Override
    protected FeedFMMediaPlayer doInBackground(Object... params) {
        Log.i(TAG, String.format("%s, doInBackground", getQueueManager().getIdentifier()));

        try {
            // When retrying this task, the Play information might already have been collected.
            if (mPlay == null) {
                mPlay = mWebservice.getPlay(mClientId, mPlayInfo.getPlacement(), mPlayInfo.getStation(),
                        Configuration.DEFAULT_AUDIO_FORMAT, // For now don't put in the AudioFormat
                        Configuration.DEFAULT_BITRATE);
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onMetaDataLoaded(TuneTask.this, mPlay);
                    }
                }
            });

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
            cancel(new FeedFMError(PlayerErrorEnum.TUNE_IO_EXCEPTION));
        } catch (IllegalStateException e) {
            // This exception is called if the MediaPlayer dataSource is set while the media player is in an invalid state.
            e.printStackTrace();
            cancel(new FeedFMError(PlayerErrorEnum.TUNE_MEDIA_PLAYER_ILLEGAL_STATE));
        }

        return mMediaPlayer;
    }

    @Override
    protected void onTaskFinished(FeedFMMediaPlayer feedFMMediaPlayer) {
        Log.i(TAG, String.format(">>>>>>>>>>>>>>>>>>>...MediaPlayer.preparing: %s", mPlay.getAudioFile().getTrack().getTitle()));

        Log.i(TAG, String.format("%s, onTaskFinished", getQueueManager()));

        if (feedFMMediaPlayer != null) {
            feedFMMediaPlayer.setOnErrorListener(null);
            mMediaPlayerPool.putTunedMediaPlayer(feedFMMediaPlayer);

            if (mListener != null) {
                mListener.onSuccess(this, feedFMMediaPlayer, feedFMMediaPlayer.getPlay());
            }
        }

        cleanup();
    }

    @Override
    protected void onPostExecute(FeedFMMediaPlayer mediaPlayer) {
        super.onPostExecute(mediaPlayer);
    }

    @Override
    protected void onTaskCancelled(FeedFMError error, int attempt) {
        Log.d(TAG, "TASK CANCELLED: " + toString());
        if (error != null) {
            if (error.isPlayerError()) {
                switch (error.getPlayerError()) {
                    case NO_NETWORK:
                        if (getAttemptCount() < Configuration.MAX_TASK_RETRY_ATTEMPTS) {
                            // Retry Tuning once we have a connection
                            getQueueManager().offerFirst(copy(attempt + 1));
                        } else if (mListener != null) {
                            mListener.onApiError(this, error);
                        }
                        break;
                    case TUNE_UNKNOWN:
                        if (mListener != null) {
                            mListener.onUnkownError(this, error);
                        }
                        break;
                    default:
                        break;
                }
            } else if (error.isApiError() && mListener != null) {
                mListener.onApiError(this, error);
            }
        }

        if (mMediaPlayer != null) {
            mMediaPlayer.setOnErrorListener(null);
            mMediaPlayerPool.release(mMediaPlayer);
            mMediaPlayer = null;
        }

        cleanup();
    }

    @Override
    public PlayerAbstractTask copy(int attempts) {
        PlayerAbstractTask task = new TuneTask(mContext, getQueueManager(), mWebservice, mMediaPlayerPool, mListener, mPlayInfo, mPlay, mClientId);
        task.setAttemptCount(attempts);
        return task;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPrepared = true;
    }

    @Override
    public Play getPlay() {
        return mPlay;
    }

    @Override
    public boolean isSkippableCandidate() {
        FeedFMError error = getError();
        // Only allow skip if there is an important error.
        return mPlay != null && error != null && error.isPlayerError() && error.getPlayerError() == PlayerErrorEnum.TUNE_UNKNOWN;
    }

    @Override
    public Integer getElapsedTimeMillis() {
        return 0;
    }

    @Override
    public String getTag() {
        return TuneTask.class.getSimpleName();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        FeedFMMediaPlayer mediaPlayer = (FeedFMMediaPlayer) mp;
        Log.e(TAG, String.format("error playing track: [%s]: (%d, %d)", mediaPlayer.getPlay().getAudioFile().getTrack().getTitle(), what, extra));

        if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            FeedFMError error = null;

            ConnectivityManager cm =
                    (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            if (!isConnected) {
                error = new FeedFMConnectivityError();
            } else {
                error = new FeedFMError(PlayerErrorEnum.TUNE_UNKNOWN);
            }
            Log.e(TAG, error.toString());

            cancel(error);
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

    private void cleanup() {
        mMediaPlayer = null;
    }

    @Override
    public String toString() {
        return String.format("{%s - isCancelled: %s}", TuneTask.class.getSimpleName(), isCancelled());
    }
}
