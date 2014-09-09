package fm.feed.android.playersdk.service.util;

import android.media.AudioManager;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import fm.feed.android.playersdk.service.FeedFMMediaPlayer;

/**
 * Created by mharkins on 9/4/14.
 */
public class MediaPlayerPool {
    private static final String TAG = MediaPlayerPool.class.getSimpleName();

    private boolean mShouldDuckVolume = false;

    Queue<FeedFMMediaPlayer> mFree = new LinkedList<FeedFMMediaPlayer>();
    Queue<FeedFMMediaPlayer> mTuning = new LinkedList<FeedFMMediaPlayer>();
    Queue<FeedFMMediaPlayer> mTuned = new LinkedList<FeedFMMediaPlayer>();
    Queue<FeedFMMediaPlayer> mPlaying = new LinkedList<FeedFMMediaPlayer>();

    public FeedFMMediaPlayer getTuningMediaPlayer() {
        synchronized (this) {
            FeedFMMediaPlayer mediaPlayer = mFree.poll();
            if (mediaPlayer == null) {
                mediaPlayer = spawn();
            }
            mTuning.offer(mediaPlayer);
            return mediaPlayer;
        }
    }

    public boolean putTunedMediaPlayer(FeedFMMediaPlayer mediaPlayer) {
        synchronized (this) {
            if (mTuning.remove(mediaPlayer)) {
                mTuned.offer(mediaPlayer);
                return true;
            }

            Log.w(TAG, String.format("Media Player %s could not be found in the TuningPool"));
            return false;
        }
    }

    public boolean hasTunedMediaPlayer() {
        synchronized (this) {
            return !mTuned.isEmpty();
        }
    }

    public FeedFMMediaPlayer getTunedMediaPlayer() {
        synchronized (this) {
            FeedFMMediaPlayer mediaPlayer = mTuned.poll();
            if (mediaPlayer != null) {
                mPlaying.offer(mediaPlayer);
                return mediaPlayer;
            }

            Log.w(TAG, String.format("No Tuned MediaPlayer available for playing."));
            return null;
        }
    }

    public void free(final FeedFMMediaPlayer mediaPlayer) {
        synchronized (this) {
            mTuning.remove(mediaPlayer);
            mTuned.remove(mediaPlayer);
            mPlaying.remove(mediaPlayer);
            mFree.remove(mediaPlayer);

            // Reset the media player on a separate Thread.
            Thread thread = new Thread()
            {
                @Override
                public void run() {
                    mediaPlayer.reset();
                    mFree.offer(mediaPlayer);
                }
            };

            thread.start();
        }
    }

    public void release(final FeedFMMediaPlayer mediaPlayer) {
        synchronized (this) {
            mTuning.remove(mediaPlayer);
            mTuned.remove(mediaPlayer);
            mPlaying.remove(mediaPlayer);
            mFree.remove(mediaPlayer);

            // Release the media player on a separate Thread.
            Thread thread = new Thread()
            {
                @Override
                public void run() {
                    mediaPlayer.release();
                }
            };

            thread.start();
        }
    }

    private FeedFMMediaPlayer spawn() {
        Log.i(TAG, "Spawning new Media Player instance");

        FeedFMMediaPlayer mediaPlayer = new FeedFMMediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        if (mShouldDuckVolume) {
            mediaPlayer.setVolume(0.1f, 0.1f);
        }

        /*
        //TODO: WIFI Lock
        WifiLock wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        wifiLock.acquire();
        ...
        wifiLock.release();
         */


        return mediaPlayer;
    }

    public void duckVolume() {
        mShouldDuckVolume = true;


        Queue[] queues = new Queue[] { mFree, mPlaying, mTuned, mTuning };
        for (Queue q: queues) {
            for (Object o: q) {
                FeedFMMediaPlayer mediaPlayer = (FeedFMMediaPlayer) o;
                mediaPlayer.setVolume(0.1f, 0.1f);
            }
        }
    }

    public void restoreVolume() {
        mShouldDuckVolume = false;


        Queue[] queues = new Queue[] { mFree, mPlaying, mTuned, mTuning };
        for (Queue q: queues) {
            for (Object o: q) {
                FeedFMMediaPlayer mediaPlayer = (FeedFMMediaPlayer) o;
                mediaPlayer.setVolume(1.0f, 1.0f);
            }
        }
    }

    public void release() {
        Queue[] queues = new Queue[] { mFree, mPlaying, mTuned, mTuning };
        for (Queue q: queues) {
            for (Object o: q) {
                FeedFMMediaPlayer mediaPlayer = (FeedFMMediaPlayer) o;
                mediaPlayer.release();
            }
            q.clear();
        }
    }
}
