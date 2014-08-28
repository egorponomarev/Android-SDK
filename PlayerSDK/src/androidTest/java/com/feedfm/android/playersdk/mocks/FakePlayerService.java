package com.feedfm.android.playersdk.mocks;

import android.util.Log;

import com.feedfm.android.playersdk.service.MediaPlayerManager;
import com.feedfm.android.playersdk.service.webservice.PlayerService;
import com.feedfm.android.playersdk.service.webservice.Webservice;

/**
 * Created by mharkins on 8/27/14.
 */
public class FakePlayerService extends PlayerService{
    public void setWebservice(Webservice webservice) {
        mWebservice = webservice;
    }

    public void setMediaPlayerManager(MediaPlayerManager mediaPlayerManager) {
        mMediaPlayerManager = mediaPlayerManager;
    }

    public String getClientIdString() {
        return mClientId;
    }

    public void setEventBus(DummyBus bus) {
        eventBus = bus;
    }

    public boolean getCanSkip() {
        return mCanSkip;
    }
}
