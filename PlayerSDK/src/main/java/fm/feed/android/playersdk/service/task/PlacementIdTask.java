package fm.feed.android.playersdk.service.task;

import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.service.TaskQueueManager;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;
import fm.feed.android.playersdk.service.webservice.model.PlayerInfo;

/**
 * Created by mharkins on 9/2/14.
 */
public abstract class PlacementIdTask extends NetworkAbstractTask<Object, Void, Placement> {
    public Integer mPlacementId;
    public PlayerInfo mPlayerInfo;

    public PlacementIdTask(TaskQueueManager queueManager, Webservice mWebservice, PlayerInfo playerInfo, Integer placementId) {
        super(queueManager, mWebservice);
        this.mPlacementId = placementId;
        this.mPlayerInfo = playerInfo;
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
        onPlacementChanged(placement);
    }

    @Override
    protected void onTaskCancelled() {

    }

    public abstract void onPlacementChanged(Placement placement);

    @Override
    public String toString() {
        return String.format("%s", PlacementIdTask.class.getSimpleName());
    }
}
