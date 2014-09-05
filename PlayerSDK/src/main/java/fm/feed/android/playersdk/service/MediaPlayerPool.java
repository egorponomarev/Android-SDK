package fm.feed.android.playersdk.service;

import android.media.AudioManager;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by mharkins on 9/4/14.
 */
public class MediaPlayerPool {
    private static final String TAG = MediaPlayerPool.class.getSimpleName();

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

    public void free(FeedFMMediaPlayer mediaPlayer) {
        synchronized (this) {
            mTuning.remove(mediaPlayer);
            mTuned.remove(mediaPlayer);
            mPlaying.remove(mediaPlayer);

            if (!mFree.contains(mediaPlayer)) {
                mFree.offer(mediaPlayer);
                mediaPlayer.reset();
            }
        }
    }

    private FeedFMMediaPlayer spawn() {
        Log.i(TAG, "Spawning new Media Player instance");

        FeedFMMediaPlayer mediaPlayer = new FeedFMMediaPlayer();
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
