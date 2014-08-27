package com.feedfm.android.playersdk.mocks;

import android.content.Context;

import com.feedfm.android.playersdk.service.webservice.Webservice;

/**
 * Created by mharkins on 8/27/14.
 */
public class FakeWebservice extends Webservice{
    public FakeWebservice(Context context) {
        super(context);
    }

    public void setRestService(RestInterface restService) {
        mRestService = restService;
    }
}
