package fm.feed.android.playersdk.service;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.util.Log;

import fm.feed.android.playersdk.model.Play;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by mharkins on 8/25/14.
 */
public class MediaPlayerManager implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener {
    private static final String TAG = MediaPlayerManager.class.getSimpleName();

    private Context mContext;
    private Listener mListener;

    private Queue<FeedFMMediaPlayer> mMediaPlayerPool;
    private Queue<FeedFMMediaPlayer> mQueue;

    private FeedFMMediaPlayer mTuningMediaPlayer;

    public MediaPlayerManager(Context context) {
        this.mContext = context;

        mMediaPlayerPool = new LinkedList<FeedFMMediaPlayer>();
        mQueue = new LinkedList<FeedFMMediaPlayer>();

//        initAudioManager();
    }

    public void setListener(Listener mListener) {
        this.mListener = mListener;
    }

    /**
     * Called to release the memory of all the {@link fm.feed.android.playersdk.service.FeedFMMediaPlayer}s
     * referenced by the {@link fm.feed.android.playersdk.service.MediaPlayerManager}
     */
    public void release() {
        FeedFMMediaPlayer mediaPlayer;
        while ((mediaPlayer = mQueue.poll()) != null) {
            mediaPlayer.release();
        }
        while ((mediaPlayer = mMediaPlayerPool.poll()) != null) {
            mediaPlayer.release();
        }
        if (mTuningMediaPlayer != null) {
            mTuningMediaPlayer.release();
            mTuningMediaPlayer = null;
        }
    }

    /**
     * Recycle the {@link fm.feed.android.playersdk.service.FeedFMMediaPlayer} currently being tuned.
     */
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
    public void tune(Play play) throws IllegalStateException {
        FeedFMMediaPlayer mediaPlayer = mMediaPlayerPool.poll();
        if (mediaPlayer == null) {
            mediaPlayer = initNewMediaPlayer();
        }

        mediaPlayer.setState(FeedFMMediaPlayer.State.FETCHING_METADATA);

        try {
            // TODO: Perhaps setDataSource and prepareAsync could be added to the setPlay method in the CustomMediaPlayer object.

            mediaPlayer.setDataSource(play.getAudioFile().getUrl());
            mediaPlayer.prepare();

            mQueue.offer(mediaPlayer);
        } catch (IOException e) {
            // TODO-XX handle otherwise.
            e.printStackTrace();
        }
    }

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

    /**
     * Clear the Pre-tuned media players.
     */
    public void clearPendingQueue() {
        FeedFMMediaPlayer activeMediaPlayer = getActiveMediaPlayer();
        for (FeedFMMediaPlayer mp : mQueue) {
            if (activeMediaPlayer != mp) {
                mQueue.remove(mp);
                mp.reset();
                mMediaPlayerPool.offer(mp);
            }
        }
        if (mTuningMediaPlayer != null) {
            mTuningMediaPlayer.reset();
            mMediaPlayerPool.offer(mTuningMediaPlayer);
            mTuningMediaPlayer = null;
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

    public void skip() {
        FeedFMMediaPlayer mediaPlayer = mQueue.peek();
        if (mediaPlayer == null) {
            mListener.onQueueDone();
        } else {
            if (mediaPlayer.getState() == FeedFMMediaPlayer.State.PAUSED || mediaPlayer.getState() == FeedFMMediaPlayer.State.STARTED) {
                mQueue.poll();
                mediaPlayer.silentReset();
                mMediaPlayerPool.offer(mediaPlayer);
            }
        }
    }

    /**
     * MediaPlayer Listener Implementations
     */
    @Override
    public void onPrepared(MediaPlayer mp) {

//        mListener.onPrepared((FeedFMMediaPlayer) mp);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        /*
        We need to attempt to recover from the error.
         */

        FeedFMMediaPlayer mediaPlayer = (FeedFMMediaPlayer) mp;

        mQueue.remove(mediaPlayer);

        // Retrieve the play information of the current play.
        boolean isActiveMediaPlayer = getActiveMediaPlayer() == mediaPlayer;
        Play savedPlay = mediaPlayer.getPlay();

        Log.e(TAG, String.format("error playing track: [%s]: (%d, %d)", savedPlay.getAudioFile().getTrack().getTitle(), what, extra));

        if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED || what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer.release();
        } else {
            mediaPlayer.silentReset();
            mMediaPlayerPool.add(mediaPlayer);
        }

        // Check if the error came from the running media player. If so, we'll need to resume where we were with the track.
        if (isActiveMediaPlayer) {
            FeedFMMediaPlayer.State savedState = mediaPlayer.getPrevState();
            boolean savedAutoPlay = mediaPlayer.isAutoPlay();

            switch (savedState) {
                case IDLE:
                case FETCHING_METADATA:
                case STOPPED:
                case COMPLETE:
                case END:
                case ERROR:
                    break;
                // In case we had the information for the play, re-tune and auto-play again.
                case INITIALIZED:
                case PREPARING:
                case PREPARED:
                case STARTED:
                case PAUSED:
                    if (savedAutoPlay) {
                        tune(savedPlay);
                    }
                    break;
            }
        }


        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        FeedFMMediaPlayer mediaPlayer = (FeedFMMediaPlayer) mp;

        mQueue.remove(mediaPlayer);
        mp.reset();
        mMediaPlayerPool.add(mediaPlayer);

        mListener.onPlayCompleted(mediaPlayer.getPlay(), mediaPlayer.isSkipped());
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        FeedFMMediaPlayer mediaPlayer = (FeedFMMediaPlayer) mp;
        if (mediaPlayer.getState() == FeedFMMediaPlayer.State.STARTED || mediaPlayer.getState() == FeedFMMediaPlayer.State.PAUSED) {
            mListener.onBufferingUpdate(mediaPlayer.getPlay(), percent);
        }
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
                        // You must skip all audio playback.
                        // Because you should expect not to have focus back for a long time, this would be a good place to clean up your resources as much as possible.
                        // For example, you should release the MediaPlayer.
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        // You have temporarily lost audio focus, but should receive it back shortly.
                        // You must skip all audio playback, but you can keep your resources because you will probably get focus back shortly.
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
            // Lost focus for an unbounded amount of time: skip playback and release media player
            if (mMediaPlayer.isPlaying()) mMediaPlayer.skip();
            mMediaPlayer.release();
            mMediaPlayer = null;
            break;

        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            // Lost focus for a short time, but we have to skip
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


    /**
     * Implement this interface to listen to {@link MediaPlayerManager} events.
     */
    public interface Listener {
        /**
         * Called when a {@link fm.feed.android.playersdk.model.Play} starts
         *
         * @param play {@link fm.feed.android.playersdk.model.Play}
         */
        public void onPlayStart(Play play);

        /**
         * Called when a play is done.
         *
         * @param play      {@link fm.feed.android.playersdk.model.Play}
         * @param isSkipped {@code true} if the play has been skipped.
         */
        public void onPlayCompleted(Play play, boolean isSkipped);

        /**
         * Called when there are no longer items to play.
         */
        public void onQueueDone();

        /**
         * Called as the {@link fm.feed.android.playersdk.model.Play} is being buffered by the {@link fm.feed.android.playersdk.service.FeedFMMediaPlayer}
         *
         * @param play    {@link fm.feed.android.playersdk.model.Play}
         * @param percent
         */
        public void onBufferingUpdate(Play play, int percent);
    }
}
