package com.feedfm.android.playersdk.mocks;

import com.feedfm.android.playersdk.service.MediaPlayerManager;
import com.feedfm.android.playersdk.service.PlayerService;
import com.feedfm.android.playersdk.service.webservice.Webservice;
import com.squareup.otto.Bus;

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
        return mPlayerInfo.getClientId();
    }

    public void setEventBus(Bus bus) {
        eventBus = bus;
    }

    public boolean getCanSkip() {
        return mPlayerInfo.isSkippable();
    }
}
