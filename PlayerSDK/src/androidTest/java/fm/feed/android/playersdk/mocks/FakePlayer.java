package fm.feed.android.playersdk.mocks;

import android.content.Context;

import fm.feed.android.playersdk.Player;
import com.squareup.otto.Bus;

/**
 * Created by mharkins on 8/27/14.
 */
public class FakePlayer extends Player {

    private FakePlayerService mServiceMock;

    private FakePlayer(Context context, PlayerListener playerListener, String token, String secret) {
        super(context, playerListener, "", "", -1);
    }

    public static FakePlayer getInstance(Context context, PlayerListener playerListener, String token, String secret) {
        return new FakePlayer(context, playerListener, token, secret);
    }

    public void setEventBus(Bus bus) {
        mEventBus = bus;
    }

    public PlayerServiceListener getPrivateServiceListener() {
        return mPrivateServiceListener;
    }
}
