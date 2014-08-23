package com.feedfm.android.playersdk.service.webservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.feedfm.android.playersdk.model.Placement;
import com.feedfm.android.playersdk.model.Station;
import com.feedfm.android.playersdk.service.OutStationWrap;
import com.feedfm.android.playersdk.service.bus.Credentials;
import com.feedfm.android.playersdk.service.bus.SingleEventBus;
import com.feedfm.android.playersdk.service.bus.StatusMessage;
import com.feedfm.android.playersdk.service.webservice.model.FeedFMError;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

/**
 * Created by mharkins on 8/21/14.
 */
public class PlayerService extends Service {
    public static final String TAG = PlayerService.class.getSimpleName();

    private static Bus eventBus = SingleEventBus.getInstance();

    private Webservice mWebservice;

    // Client State data
    private String mClientId;
    private List<Station> mStationList;

    private Placement mSelectedPlacement;
    private Station mSelectedStation;

    @Override
    public void onCreate() {
        super.onCreate();

        mWebservice = new Webservice(this);

        eventBus.register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        eventBus.post(new StatusMessage(StatusMessage.Status.STARTED));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void setCredentials(Credentials credentials) {
        mWebservice.setCredentials(credentials);

        getClientId();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void setPlacementId(Placement placement) {
        mWebservice.setPlacementId(placement.getId(), new Webservice.Callback<Pair<Placement, List<Station>>>() {
            @Override
            public void onSuccess(Pair<Placement, List<Station>> result) {
                // Save user Placement
                mSelectedPlacement = result.first;
                mStationList = result.second;

                eventBus.post(result);
            }

            @Override
            public void onFailure(FeedFMError error) {
                Toast.makeText(PlayerService.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void setStationId(OutStationWrap wrapper) {
        Station station = wrapper.getObject();
        if (mStationList != null) {
            for (Station s : mStationList) {
                if (s.getId() == station.getId()) {
                    mSelectedStation = s;
                    eventBus.post(mSelectedStation);
                    return;
                }
            }
        }
        Log.w(TAG, String.format("Station %d could not be found for current placement.", station.getId()));
    }

    public void getClientId() {
        mWebservice.getClientId(new Webservice.Callback<String>() {
            @Override
            public void onSuccess(String clientId) {
                Toast.makeText(PlayerService.this, "Client Id: " + clientId, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(FeedFMError error) {
                Toast.makeText(PlayerService.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
