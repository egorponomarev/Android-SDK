package com.feedfm.android.playersdk.mocks;

import android.util.Pair;

import com.feedfm.android.playersdk.model.Placement;
import com.feedfm.android.playersdk.model.Station;
import com.feedfm.android.playersdk.service.bus.Credentials;
import com.feedfm.android.playersdk.service.bus.EventMessage;
import com.feedfm.android.playersdk.service.bus.OutStationWrap;
import com.feedfm.android.playersdk.service.bus.PlayerAction;
import com.feedfm.android.playersdk.service.bus.SingleEventBus;

import java.util.List;

/**
 * Created by mharkins on 8/27/14.
 */
public class FakeBus extends SingleEventBus {
    private FakePlayer mPlayer;
    private FakePlayerService mService;

    public FakeBus(FakePlayer player, FakePlayerService service) {
        super();
        this.mPlayer = player;
        this.mService = service;

        this.mPlayer.setEventBus(this);
        this.mService.setEventBus(this);
    }


    public void post(Object object) {
        if (object instanceof Credentials) {
            mService.setCredentials((Credentials) object);
        } else if (object instanceof Placement) {
            mService.setPlacementId((Placement) object);
        }else if (object instanceof OutStationWrap) {
            mService.setStationId((OutStationWrap) object);
        }else if (object instanceof PlayerAction) {
            mService.onPlayerAction((PlayerAction) object);
        }else if (object instanceof EventMessage) {
            mPlayer.getPrivateServiceListener().onServiceStatusChange((EventMessage) object);
        }else if (object instanceof Pair) {
            mPlayer.getPrivateServiceListener().onPlacementChanged((Pair<Placement, List<Station>>) object);
        }else if (object instanceof Station) {
            mPlayer.getPrivateServiceListener().onStationChanged((Station) object);
        }
    }
}
