package fm.feed.android.playersdk.mocks;

import android.util.Pair;

import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.bus.Credentials;
import fm.feed.android.playersdk.service.bus.EventMessage;
import fm.feed.android.playersdk.service.bus.OutStationWrap;
import fm.feed.android.playersdk.service.bus.PlayerAction;
import fm.feed.android.playersdk.service.bus.BusProvider;
import com.squareup.otto.Bus;

import java.util.List;

/**
 * Created by mharkins on 8/27/14.
 */
public class DummyBusProvider extends BusProvider {
    protected static Bus newInstance() {
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
                mService.setPlacementId((Placement) object);
            } else if (object instanceof OutStationWrap) {
                mService.setStationId((OutStationWrap) object);
            } else if (object instanceof PlayerAction) {
                mService.onPlayerAction((PlayerAction) object);
            } else if (object instanceof EventMessage) {
                mPlayer.getPrivateServiceListener().onServiceStatusChange((EventMessage) object);
            } else if (object instanceof Pair) {
                mPlayer.getPrivateServiceListener().onPlacementChanged((Pair<Placement, List<Station>>) object);
            } else if (object instanceof Station) {
                mPlayer.getPrivateServiceListener().onStationChanged((Station) object);
            }
        }
    }
}
