package fm.feed.android.playersdk.service.task;

import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.webservice.model.PlayerInfo;

/**
 * Created by mharkins on 9/2/14.
 */
public class StationIdTask extends PlayerAbstractTask<Object, Void, Station> {
    public interface OnStationIdChanged {
        public void onSuccess(Station station);
    }

    private OnStationIdChanged mListener;

    public Integer mStationId;
    public PlayerInfo mPlayerInfo;

    public StationIdTask(TaskQueueManager queueManager, OnStationIdChanged mListener, PlayerInfo mPlayerInfo, Integer mStationId) {
        super(queueManager);
        this.mListener = mListener;
        this.mStationId = mStationId;
        this.mPlayerInfo = mPlayerInfo;
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
        if (this.mListener != null) {
            this.mListener.onSuccess(station);
        }
    }

    @Override
    protected void onTaskCancelled() {

    }

    @Override
    public String toString() {
        return String.format("%s", StationIdTask.class.getSimpleName());
    }
}
