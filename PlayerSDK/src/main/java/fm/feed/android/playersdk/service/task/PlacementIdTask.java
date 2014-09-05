package fm.feed.android.playersdk.service.task;

import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.service.TaskQueueManager;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;
import fm.feed.android.playersdk.service.webservice.model.PlayerInfo;

/**
 * Created by mharkins on 9/2/14.
 */
public class PlacementIdTask extends NetworkAbstractTask<Object, Void, Placement> {
    public interface OnPlacementIdChanged {
        public void onSuccess(Placement placement);
    }

    public Integer mPlacementId;
    public PlayerInfo mPlayerInfo;

    private OnPlacementIdChanged mListener;

    public PlacementIdTask(TaskQueueManager queueManager, Webservice mWebservice, OnPlacementIdChanged listener, PlayerInfo playerInfo, Integer placementId) {
        super(queueManager, mWebservice);
        this.mPlacementId = placementId;
        this.mPlayerInfo = playerInfo;
        this.mListener = listener;
    }

    @Override
    protected Placement doInBackground(Object... params) {
        Placement placement = null;
        try {
            // Perform Synchronous Webservice Request.
            placement = mWebservice.setPlacementId(mPlacementId);

            mPlayerInfo.setStationList(placement.getStationList());

            boolean didChangePlacement =
                    mPlayerInfo.getPlacement() == null ||
                            !mPlayerInfo.getPlacement().getId().equals(placement.getId());
            if (didChangePlacement) {
                // Save user Placement
                mPlayerInfo.setPlacement(placement);
                mPlayerInfo.setStation(null);
            }

        } catch (FeedFMError feedFMError) {
            feedFMError.printStackTrace();
        }
        return placement;
    }

    @Override
    protected void onTaskFinished(Placement placement) {
        if (mListener != null) {
            mListener.onSuccess(placement);
        }
    }

    @Override
    protected void onTaskCancelled() {

    }

    @Override
    public String toString() {
        return String.format("%s", PlacementIdTask.class.getSimpleName());
    }
}
