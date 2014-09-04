package com.feedfm.android.playersdk.service.task;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.feedfm.android.playersdk.model.Play;
import com.feedfm.android.playersdk.service.FeedFMMediaPlayer;
import com.feedfm.android.playersdk.service.TaskQueueManager;
import com.feedfm.android.playersdk.service.webservice.Webservice;
import com.feedfm.android.playersdk.service.webservice.model.FeedFMError;
import com.feedfm.android.playersdk.service.webservice.model.PlayerInfo;

import java.io.IOException;

/**
 * Created by mharkins on 9/2/14.
 */
public abstract class TuneTask extends MediaPlayerAbstractTask<Object, Void, FeedFMMediaPlayer> implements MediaPlayer.OnErrorListener {
    private static final String TAG = TuneTask.class.getSimpleName();

    private PlayerInfo mPlayerInfo;

    protected TuneTask(TaskQueueManager queueManager, Webservice mWebservice, PlayerInfo playerInfo) {
        super(queueManager, mWebservice);

        this.mPlayerInfo = playerInfo;
    }

    @Override
    protected FeedFMMediaPlayer doInBackground(Object... params) {
        Log.i(TAG, String.format("%s, doInBackground", getQueueManager().getIdentifier()));

        FeedFMMediaPlayer mediaPlayer = null;
        // This exception is called if the MediaPlayer dataSource is set while the media player is in an invalid state.
        try {
            mediaPlayer = initNewMediaPlayer();
            mediaPlayer.setState(FeedFMMediaPlayer.State.FETCHING_METADATA);

            Play play = mWebservice.getPlay(mPlayerInfo.getClientId(), mPlayerInfo.getPlacement(), mPlayerInfo.getStation(),
                    null, // For now don't put in the AudioFormat
                    null);

            mediaPlayer.setPlay(play);
            mediaPlayer.setDataSource(play.getAudioFile().getUrl());
            Log.i(TAG, "MediaPlayer.prepare....");
            mediaPlayer.prepare();
            Log.i(TAG, "...MediaPlayer.prepare");

        } catch (FeedFMError feedFMError) {
            feedFMError.printStackTrace();
        } catch (IOException e) {
            // TODO-XX handle otherwise.
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            // TODO: handle fail
        }

        return mediaPlayer;
    }

    @Override
    protected void onTaskFinished(FeedFMMediaPlayer feedFMMediaPlayer) {
        Log.i(TAG, String.format("%s, onTaskFinished", getQueueManager()));

        onTuned(feedFMMediaPlayer, feedFMMediaPlayer.getPlay());
    }

    @Override
    protected void onTaskCancelled() {

    }

    public abstract void onTuned(FeedFMMediaPlayer mediaPlayer, Play play);

    /**
     * Create and configure a new CustomMediaPlayer instance
     *
     * @return
     */
    protected FeedFMMediaPlayer initNewMediaPlayer() {
        FeedFMMediaPlayer mediaPlayer = new FeedFMMediaPlayer();
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        /*
        //TODO: WIFI Lock
        WifiLock wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        wifiLock.acquire();
        ...
        wifiLock.release();
         */


        return mediaPlayer;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        FeedFMMediaPlayer mediaPlayer = (FeedFMMediaPlayer) mp;
        Log.e(TAG, String.format("error playing track: [%s]: (%d, %d)", mediaPlayer.getPlay().getAudioFile().getTrack().getTitle(), what, extra));
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s", TuneTask.class.getSimpleName());
    }
}
