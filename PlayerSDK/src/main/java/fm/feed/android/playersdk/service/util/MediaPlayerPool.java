package fm.feed.android.playersdk.service.util;

import android.media.AudioManager;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import fm.feed.android.playersdk.service.FeedFMMediaPlayer;

/**
 * This class holds MediaPlayer instances. The instances that it holds
 * are considered to be in one of 4 states:
 *
 *   - free = available to load and play something
 *   - tuning = currently loading a remote URL
 *   - tuned = all ready for playback
 *   - playing = currently playing audio
 *
 * You can 'getTuningMediaPlayer()', which will retrieve a free player, mark
 * it as 'tuning', and then return it. When that player has transitioned
 * to the 'prepared' state (according to the MediaPlayer docs),
 * it should be handed back to the pool, where it is considered 'tuned'.
 * You can then 'getTunedMediaPlayer()', which is all ready for music
 * playback. When you are done with the player, it should be 'free()'d
 * so that it is thrown back in the 'free' state and available for
 * reuse.
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
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
            Thread thread = new Thread() {
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
            Thread thread = new Thread() {
                @Override
                public void run() {
                    mediaPlayer.release();
                }
            };

            thread.start();
        }
    }

    /**
     * Resets all the media players
     */
    public void releaseTunedPlayers() {
        synchronized (this) {
            Queue[] queues = new Queue[]{mTuned};
            for (Queue q : queues) {
                for (Object o : q) {
                    FeedFMMediaPlayer mediaPlayer = (FeedFMMediaPlayer) o;
                    mediaPlayer.release();
                }
                q.clear();
            }
        }
    }

    /**
     * Instantiates a new {@link fm.feed.android.playersdk.service.FeedFMMediaPlayer} instance.
     * <p>Makes it quiet if the Audio Focus is set as Should Duck</p>
     *
     * @return a {@link fm.feed.android.playersdk.service.FeedFMMediaPlayer}
     */
    protected FeedFMMediaPlayer spawn() {
        Log.i(TAG, "Spawning new Media Player instance");

        FeedFMMediaPlayer mediaPlayer = new FeedFMMediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        if (mShouldDuckVolume) {
            mediaPlayer.setVolume(0.1f, 0.1f);
        }

        return mediaPlayer;
    }

    public void duckVolume() {
        mShouldDuckVolume = true;


        Queue[] queues = new Queue[]{mFree, mPlaying, mTuned, mTuning};
        for (Queue q : queues) {
            for (Object o : q) {
                FeedFMMediaPlayer mediaPlayer = (FeedFMMediaPlayer) o;
                mediaPlayer.setVolume(0.1f, 0.1f);
            }
        }
    }

    public void restoreVolume() {
        mShouldDuckVolume = false;


        Queue[] queues = new Queue[]{mFree, mPlaying, mTuned, mTuning};
        for (Queue q : queues) {
            for (Object o : q) {
                FeedFMMediaPlayer mediaPlayer = (FeedFMMediaPlayer) o;
                mediaPlayer.setVolume(1.0f, 1.0f);
            }
        }
    }

    public void release() {
        Queue[] queues = new Queue[]{mFree, mPlaying, mTuned, mTuning};
        for (Queue q : queues) {
            for (Object o : q) {
                FeedFMMediaPlayer mediaPlayer = (FeedFMMediaPlayer) o;
                mediaPlayer.release();
            }
            q.clear();
        }
    }
}
