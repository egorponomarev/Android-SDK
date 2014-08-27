package com.feedfm.android.playersdk.service.bus;

import com.squareup.otto.Bus;

/**
 * Created by mharkins on 8/21/14.
 */
public class SingleEventBus extends Bus {
    private static SingleEventBus mInstance;

    protected SingleEventBus() {

    }

    public static SingleEventBus getInstance() {
        if (mInstance == null) {
            mInstance = new SingleEventBus();
        }
        return mInstance;
    }
}
