package com.feedfm.android.playersdk.mocks;

import android.content.Context;

import com.feedfm.android.playersdk.Player;

/**
 * Created by mharkins on 8/27/14.
 */
public class FakePlayer extends Player {

    private FakePlayerService mServiceMock;

    private FakePlayer(Context context, ClientListener clientListener) {
        super(context, clientListener);
    }

    public static FakePlayer getInstance(Context context, ClientListener clientListener) {
        return new FakePlayer(context, clientListener);
    }

    @Override
    protected void startPlayerService(Context context) {
        // do nothing
    }

    public void setEventBus(FakeBus bus) {
        mEventBus = bus;
    }

    public PlayerServiceListener getPrivateServiceListener() {
        return mPrivateServiceListener;
    }
}
