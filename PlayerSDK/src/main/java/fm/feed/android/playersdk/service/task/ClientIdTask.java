package fm.feed.android.playersdk.service.task;

import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
 * Created by mharkins on 9/2/14.
 */
public class ClientIdTask extends NetworkAbstractTask<Object, Void, String> {
    public interface OnClientIdChanged {
        public void onSuccess(String clientId);
    }
    private OnClientIdChanged mListener;

    public ClientIdTask(TaskQueueManager queueManager, Webservice mWebservice, OnClientIdChanged listener) {
        super(queueManager, mWebservice);

        this.mListener = listener;
    }

    @Override
    protected String doInBackground(Object... params) {
        String clientId = null;
        try {
            clientId = mWebservice.getClientId();
        } catch (FeedFMError feedFMError) {
            feedFMError.printStackTrace();
        }
        return clientId;
    }

    @Override
    protected void onTaskCancelled() {

    }

    @Override
    protected void onTaskFinished(String clientId) {
        if (mListener != null) {
            mListener.onSuccess(clientId);
        }
    }

    @Override
    public String toString() {
        return String.format("%s", ClientIdTask.class.getSimpleName());
    }

}
