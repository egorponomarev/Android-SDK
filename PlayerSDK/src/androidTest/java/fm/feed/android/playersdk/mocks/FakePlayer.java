package fm.feed.android.playersdk.mocks;

import android.content.Context;

import fm.feed.android.playersdk.Player;
import com.squareup.otto.Bus;

/**
 * Created by mharkins on 8/27/14.
 */
public class FakePlayer extends Player {

    private FakePlayerService mServiceMock;

    private FakePlayer(Context context, PlayerListener playerListener, NavListener navListener, SocialListener socialListener) {
        super(context, playerListener, navListener, socialListener);
    }

    public static FakePlayer getInstance(Context context, PlayerListener playerListener, NavListener navListener, SocialListener socialListener) {
        return new FakePlayer(context, playerListener, navListener, socialListener);
    }

    @Override
    protected void startPlayerService(Context context) {
        // do nothing
    }

    public void setEventBus(Bus bus) {
        mEventBus = bus;
    }

    public PlayerServiceListener getPrivateServiceListener() {
        return mPrivateServiceListener;
    }
}