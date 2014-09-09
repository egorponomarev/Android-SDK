package fm.feed.android.playersdk.service.task;

import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.service.PlayInfo;
import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
 * Created by mharkins on 9/2/14.
 */
public class PlacementIdTask extends NetworkAbstractTask<Object, Void, Placement> {
    public interface OnPlacementIdChanged {
        public void onSuccess(Placement placement);
    }

    public Integer mPlacementId;

    private OnPlacementIdChanged mListener;

    public PlacementIdTask(TaskQueueManager queueManager, Webservice mWebservice, OnPlacementIdChanged listener, Integer placementId) {
        super(queueManager, mWebservice);
        this.mPlacementId = placementId;
        this.mListener = listener;
    }

    @Override
    protected Placement doInBackground(Object... params) {
        Placement placement = null;
        try {
            // Get the Default Placement information
            if (mPlacementId == null) {
                placement = mWebservice.getPlacementInfo();
            } else {
                placement = mWebservice.setPlacementId(mPlacementId);
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
