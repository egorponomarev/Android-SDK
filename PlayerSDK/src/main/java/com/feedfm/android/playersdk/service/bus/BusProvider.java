package com.feedfm.android.playersdk.service.bus;

import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by mharkins on 8/21/14.
 */
public class BusProvider extends Bus {
    private static final Bus mInstance = newInstance();

    protected BusProvider() {

    }

    protected static Bus newInstance() {
        return new Bus(ThreadEnforcer.ANY);
    }

    public static Bus getInstance() {
        return mInstance;
    }
}
