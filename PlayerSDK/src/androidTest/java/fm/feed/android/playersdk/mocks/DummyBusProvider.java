package fm.feed.android.playersdk.mocks;

import android.util.Pair;

import com.squareup.otto.Bus;

import java.util.List;

import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.bus.BusProvider;
import fm.feed.android.playersdk.service.bus.Credentials;
import fm.feed.android.playersdk.service.bus.EventMessage;
import fm.feed.android.playersdk.service.bus.OutPlacementWrap;
import fm.feed.android.playersdk.service.bus.OutStationWrap;
import fm.feed.android.playersdk.service.bus.PlayerAction;

/**
 * Created by mharkins on 8/27/14.
 */
public class DummyBusProvider extends BusProvider {
    public static Bus getInstance() {
        return new MockBus();
    }

    public static class MockBus extends Bus {
        FakePlayer mPlayer;
        FakePlayerService mService;

        public void setPlayer(FakePlayer mPlayer) {
            this.mPlayer = mPlayer;
            this.mPlayer.setEventBus(this);
        }

        public void setService(FakePlayerService mService) {
            this.mService = mService;
            this.mService.setEventBus(this);
        }

        public void post(Object object) {
            if (object instanceof Credentials) {
                mService.setCredentials((Credentials) object);
            } else if (object instanceof Placement) {
                mService.setPlacementId(new OutPlacementWrap((Placement) object));
            } else if (object instanceof OutStationWrap) {
                mService.setStationId((OutStationWrap) object);
            } else if (object instanceof PlayerAction) {
                mService.onPlayerAction((PlayerAction) object);
            } else if (object instanceof EventMessage) {
                mPlayer.getPrivateServiceListener().onServiceStatusChange((EventMessage) object);
            } else if (object instanceof Pair) {
                mPlayer.getPrivateServiceListener().onPlacementChanged((Placement) object);
            } else if (object instanceof Station) {
                mPlayer.getPrivateServiceListener().onStationChanged((Station) object);
            }
        }
    }
}
