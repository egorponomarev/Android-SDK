package com.feedfm.android.playersdk.mocks;

import android.content.Context;

import com.feedfm.android.playersdk.service.FeedFMMediaPlayer;
import com.feedfm.android.playersdk.service.MediaPlayerManager;

/**
 * Created by mharkins on 8/27/14.
 */
public class FakeMediaPlayerManager extends MediaPlayerManager{

    public FakeMediaPlayerManager(Context context, Listener listener) {
        super(context, listener);
    }

    @Override
    protected FeedFMMediaPlayer initNewMediaPlayer() {
        FeedFMMediaPlayer mediaPlayer = new DummyMediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);

        return mediaPlayer;
    }
}