package com.feedfm.android.playersdk.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.feedfm.android.playersdk.model.Play;
import com.feedfm.android.playersdk.service.webservice.PlayerService;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by mharkins on 8/25/14.
 */
public class MediaPlayerManager implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private static final String TAG = MediaPlayerManager.class.getSimpleName();

    private Context mContext;
    private Listener mListener;

    private Queue<FeedFMMediaPlayer> mMediaPlayerPool = new LinkedList<FeedFMMediaPlayer>();
    private Queue<FeedFMMediaPlayer> mQueue = new LinkedList<FeedFMMediaPlayer>();

    private FeedFMMediaPlayer mTuningMediaPlayer;

    public MediaPlayerManager(Context context, Listener listener) {
        this.mContext = context;
        this.mListener = listener;

        initAudioManager();
    }

    public void preTune(boolean autoPlay) {
        mTuningMediaPlayer = mMediaPlayerPool.poll();
        if (mTuningMediaPlayer == null) {
            mTuningMediaPlayer = initNewMediaPlayer();
        }

        mTuningMediaPlayer.setState(FeedFMMediaPlayer.State.FETCHING_METADATA);
        mTuningMediaPlayer.setAutoPlay(autoPlay);
    }

    public void deTune() {
        if (mTuningMediaPlayer != null) {
            mTuningMediaPlayer.reset();
            mMediaPlayerPool.offer(mTuningMediaPlayer);
            mTuningMediaPlayer = null;
        }
    }

    /**
     * Start streaming and buffering the Play
     *
     * @param play
     */
    public void tune(Play play) {
        // Verify that we are not already tuning or playing that Play.
        // We only learn about which Play we are going to receive when we receive the data from the server.
        // The same play will be received again if the previous Play hasn't been signaled as Started yet
        FeedFMMediaPlayer mediaPlayer = getMediaPlayerForPlay(play);
        if (mediaPlayer != null) {
            // TODO: this is a bit of a patch up to have a tuned track start
            if (mTuningMediaPlayer.isAutoPlay()) {
                mediaPlayer.setAutoPlay(true);
                if (mediaPlayer.getState() == FeedFMMediaPlayer.State.PREPARED) {
                    mediaPlayer.start();
                }
            }
            mTuningMediaPlayer.reset();
            mMediaPlayerPool.offer(mTuningMediaPlayer);
            mTuningMediaPlayer = null;
            return;
        }

        mTuningMediaPlayer.setPlay(play);

        try {
            // TODO: Perhaps setDataSource and prepareAsync could be added to the setPlay method in the CustomMediaPlayer object.
            mTuningMediaPlayer.setDataSource(play.getAudioFile().getUrl());
            mTuningMediaPlayer.prepareAsync();

            mQueue.offer(mTuningMediaPlayer);
            mTuningMediaPlayer = null;
        } catch (IOException e) {
            // TODO-XX handle otherwise.
            e.printStackTrace();
        }
    }

    public FeedFMMediaPlayer getMediaPlayerForPlay(Play play) {
        for (FeedFMMediaPlayer mediaPlayer : mQueue) {
            if (mediaPlayer.getPlay().getId().equals(play.getId())) {
                return mediaPlayer;
            }
        }
        return null;
    }

    public FeedFMMediaPlayer getActiveMediaPlayer() {
        return mQueue.peek();
    }

    public FeedFMMediaPlayer.State getActiveMediaPlayerState() {
        FeedFMMediaPlayer mediaPlayer = getActiveMediaPlayer();
        if (mediaPlayer != null) {
            return mediaPlayer.getState();
        }
        return FeedFMMediaPlayer.State.IDLE;
    }

    public boolean isActiveMediaPlayer(FeedFMMediaPlayer mediaPlayer) {
        return mediaPlayer == mQueue.peek();
    }

    /**
     * Allow tuning to happen while playing the song too.
     *
     * @return
     */
    public boolean isReadyForTuning() {
        FeedFMMediaPlayer.State state = getActiveMediaPlayerState();
        return mTuningMediaPlayer == null &&
                (state == FeedFMMediaPlayer.State.IDLE ||
                        state == FeedFMMediaPlayer.State.STARTED ||
                        state == FeedFMMediaPlayer.State.PAUSED);
    }

    public boolean isReadyForPlay() {
        FeedFMMediaPlayer.State state = getActiveMediaPlayerState();
        return state == FeedFMMediaPlayer.State.PREPARED;
    }

    public boolean isPaused() {
        FeedFMMediaPlayer.State state = getActiveMediaPlayerState();
        return state == FeedFMMediaPlayer.State.PAUSED;
    }

    public boolean isPlaying() {
        FeedFMMediaPlayer.State state = getActiveMediaPlayerState();
        return state == FeedFMMediaPlayer.State.STARTED;
    }

    public void playNext() {
        FeedFMMediaPlayer mediaPlayer = mQueue.peek();
        if (mediaPlayer == null) {
            mListener.onQueueDone();
        } else {
            if (mediaPlayer.getState() == FeedFMMediaPlayer.State.PREPARED) {
                mediaPlayer.start();
                mListener.onPlayStart(mediaPlayer.getPlay());
            } else {
                // Auto Play once we get to it.
                mediaPlayer.setAutoPlay(true);
                mediaPlayer.setPlay(mediaPlayer.getPlay());
            }
        }
    }

    public void stop() {
        FeedFMMediaPlayer mediaPlayer = mQueue.peek();
        if (mediaPlayer == null) {
            mListener.onQueueDone();
        } else {
            if (mediaPlayer.getState() == FeedFMMediaPlayer.State.PAUSED || mediaPlayer.getState() == FeedFMMediaPlayer.State.STARTED) {
                mQueue.poll();
                mediaPlayer.reset();
                mMediaPlayerPool.offer(mediaPlayer);
            }
        }
    }

    /**
     * Create and configure a new CustomMediaPlayer instance
     *
     * @return
     */
    private FeedFMMediaPlayer initNewMediaPlayer() {
        int instanceCount = mQueue.size() + mMediaPlayerPool.size() + (mTuningMediaPlayer == null ? 0 : 1);
        Log.d(TAG, "New Instance of FeedFMMediaPlayer. Total: " + instanceCount);

        FeedFMMediaPlayer mediaPlayer = new FeedFMMediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
        /*
        //TODO: WIFI Lock
        WifiLock wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        wifiLock.acquire();
        ...
        wifiLock.release();
         */


        return mediaPlayer;
    }

    private void newNotification(Play play) {
        PendingIntent pi = PendingIntent.getService(mContext.getApplicationContext(), 0,
                new Intent(mContext.getApplicationContext(), PlayerService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext);
        mBuilder.setContentIntent(pi);
        mBuilder.setContentTitle("Feed.FM");
        mBuilder.setContentText("Playing: " + play.getAudioFile().getTrack().getTitle());
        mBuilder.setSmallIcon(android.R.drawable.ic_media_play);

        int NOTIFICATION_ID = 555;
        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        // NOTIFICATION_ID allows you to update the notification later on.
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * MediaPlayer Listener Implementations
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        mListener.onPrepared((FeedFMMediaPlayer) mp);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        FeedFMMediaPlayer mediaPlayer = (FeedFMMediaPlayer) mp;

        Play play = mediaPlayer.getPlay();
        Log.e(TAG, String.format("error playing track: [%s]: (%d, %d)", play.getAudioFile().getTrack().getTitle(), what, extra));

        mQueue.remove(mediaPlayer);

        if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            mp.release();
        } else {
            mp.reset();
            mMediaPlayerPool.add(mediaPlayer);
        }

        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        FeedFMMediaPlayer mediaPlayer = (FeedFMMediaPlayer) mp;

        mQueue.remove(mediaPlayer);
        mp.reset();
        mMediaPlayerPool.add(mediaPlayer);

        mListener.onPlayCompleted(mediaPlayer.getPlay());
    }

    private void initAudioManager() {

        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                // TODO: deal with different focus changes.
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        // You have gained the audio focus.
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        // You have lost the audio focus for a presumably long time.
                        // You must stop all audio playback.
                        // Because you should expect not to have focus back for a long time, this would be a good place to clean up your resources as much as possible.
                        // For example, you should release the MediaPlayer.
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        // You have temporarily lost audio focus, but should receive it back shortly.
                        // You must stop all audio playback, but you can keep your resources because you will probably get focus back shortly.
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        // You have temporarily lost audio focus, but you are
                        // allowed to continue to play audio quietly (at a low volume) instead of killing audio completely.
                        break;
                    default:
                        break;
                }

                                                /*
public void onAudioFocusChange(int focusChange) {
    switch (focusChange) {
        case AudioManager.AUDIOFOCUS_GAIN:
            // resume playback
            if (mMediaPlayer == null) initMediaPlayer();
            else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
            mMediaPlayer.setVolume(1.0f, 1.0f);
            break;

        case AudioManager.AUDIOFOCUS_LOSS:
            // Lost focus for an unbounded amount of time: stop playback and release media player
            if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            break;

        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            // Lost focus for a short time, but we have to stop
            // playback. We don't release the media player because playback
            // is likely to resume
            if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
            break;

        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
            // Lost focus for a short time, but it's ok to keep playing
            // at an attenuated level
            if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
            break;
    }
}
                                                 */
            }
        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // could not get audio focus.
        }
    }

    public void release() {
        FeedFMMediaPlayer mediaPlayer;
        while((mediaPlayer = mQueue.poll()) != null) {
            mediaPlayer.release();
        }
        while((mediaPlayer = mMediaPlayerPool.poll()) != null) {
            mediaPlayer.release();
        }
        if (mTuningMediaPlayer != null) {
            mTuningMediaPlayer.release();
            mTuningMediaPlayer = null;
        }
    }


    public interface Listener {
        public void onPrepared(FeedFMMediaPlayer mp);

        public void onPlayStart(Play play);

        public void onPlayCompleted(Play play);

        public void onQueueDone();
    }
}
