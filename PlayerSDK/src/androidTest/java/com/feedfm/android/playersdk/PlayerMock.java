package com.feedfm.android.playersdk;

import android.content.Context;

import com.squareup.otto.Bus;

/**
 * Created by mharkins on 8/27/14.
 */
public class PlayerMock extends Player{

    private PlayerMock(Context context, ClientListener clientListener) {
        super(context, clientListener);
    }

    public static Player getInstance(Context context, ClientListener clientListener, Bus eventBus) {
        Player p = PlayerMock.getInstance(context, clientListener);
        p.mEventBus = eventBus;
        return p;
    }
}
