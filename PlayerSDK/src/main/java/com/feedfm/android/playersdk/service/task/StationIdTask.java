package com.feedfm.android.playersdk.service.task;

import com.feedfm.android.playersdk.model.Station;
import com.feedfm.android.playersdk.service.TaskQueueManager;
import com.feedfm.android.playersdk.service.webservice.model.PlayerInfo;

/**
 * Created by mharkins on 9/2/14.
 */
public abstract class StationIdTask extends PlayerAbstractTask<Object, Void, Station> {
    public Integer mStationId;
    public PlayerInfo mPlayerInfo;

    public StationIdTask(TaskQueueManager queueManager, PlayerInfo playerInfo, Integer placementId) {
        super(queueManager);
        this.mStationId = placementId;
        this.mPlayerInfo = playerInfo;
    }

    @Override
    protected Station doInBackground(Object... params) {
        // If the user selects the same station, do nothing.
        boolean didChangeStation =
                mPlayerInfo.getStation() == null ||
                        !mPlayerInfo.getStation().getId().equals(mStationId);
        if (!didChangeStation) {
            return null;
        }

        if (mPlayerInfo.hasStationList()) {
            for (Station s : mPlayerInfo.getStationList()) {
                if (s.getId().equals(mStationId)) {
                    mPlayerInfo.setStation(s);

                    return s;
                }
            }
        }
        return null;
    }

    @Override
    protected void onTaskFinished(Station station) {
        onStationChanged(station);
    }

    @Override
    protected void onTaskCancelled() {

    }

    public abstract void onStationChanged(Station station);

    @Override
    public String toString() {
        return String.format("%s", StationIdTask.class.getSimpleName());
    }
}
