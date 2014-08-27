package com.feedfm.android.playersdk.mocks;

import android.util.Log;

import com.feedfm.android.playersdk.service.webservice.PlayerService;
import com.feedfm.android.playersdk.service.webservice.Webservice;

/**
 * Created by mharkins on 8/27/14.
 */
public class FakePlayerService extends PlayerService{
    public void setWebservice(Webservice webservice) {
        Log.d(TAG, "Setting Webservice");
        mWebservice = webservice;
    }

    public String getClientIdString() {
        return mClientId;
    }

    public void setEventBus(FakeBus bus) {
        eventBus = bus;
    }
}
