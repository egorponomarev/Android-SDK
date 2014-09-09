package fm.feed.android.playersdk.service.task;

import java.util.List;

import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.PlayInfo;
import fm.feed.android.playersdk.service.queue.TaskQueueManager;

/**
 * Created by mharkins on 9/2/14.
 */
public class StationIdTask extends PlayerAbstractTask<Object, Void, Station> {
    public interface OnStationIdChanged {
        public void onSuccess(Station station);
    }

    private OnStationIdChanged mListener;

    public Integer mCurrentStationId;
    public Integer mSelectedStationId;
    public List<Station> mStationList;

    public StationIdTask(TaskQueueManager queueManager, OnStationIdChanged mListener, List<Station> stationList, Integer currentStationId, Integer selectedStationId) {
        super(queueManager);
        this.mListener = mListener;
        this.mSelectedStationId = selectedStationId;
        this.mCurrentStationId = currentStationId;
        this.mStationList = stationList;
    }

    @Override
    protected Station doInBackground(Object... params) {
        // If the user selects the same station, do nothing.
        boolean didChangeStation =
                mCurrentStationId == null ||
                        !mCurrentStationId.equals(mSelectedStationId);
        if (!didChangeStation) {
            return null;
        }

        if (mStationList != null) {
            for (Station s : mStationList) {
                if (s.getId().equals(mSelectedStationId)) {
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
