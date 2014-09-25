package fm.feed.android.playersdk.mocks;

import android.content.Context;

import com.squareup.otto.Bus;

import fm.feed.android.playersdk.Player;

/**
 * Created by mharkins on 8/27/14.
 */
public class FakePlayer extends Player {

    private FakePlayerService mServiceMock;

    private DummyBusProvider mDummyBusProvider;

    private FakePlayer(Context context, Bus bus, PlayerListener playerListener, String token, String secret) {
        super(context, bus, token, secret, -1);
        registerPlayerListener(playerListener);
    }

    public static FakePlayer getInstance(Context context, Bus busProvider, PlayerListener playerListener, String token, String secret) {
        return new FakePlayer(context, busProvider, playerListener, token, secret);
    }

    @Override
    protected void startPlayerService(Context context, Integer notificationId) {
    }

    public void setEventBus(Bus bus) {
        mEventBus = bus;
    }

    public PlayerServiceListener getPrivateServiceListener() {
        return mPrivateServiceListener;
    }
}
