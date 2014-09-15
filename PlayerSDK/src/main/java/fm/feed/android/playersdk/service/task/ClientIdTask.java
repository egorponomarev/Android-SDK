package fm.feed.android.playersdk.service.task;

import fm.feed.android.playersdk.service.constant.Configuration;
import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
 * Created by mharkins on 9/2/14.
 */
public class ClientIdTask extends NetworkAbstractTask<Object, Void, String> {
    public interface OnClientIdChanged {
        public void onSuccess(String clientId);
        public void onFail(FeedFMError error);
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
            cancel(feedFMError);
        }
        return clientId;
    }

    @Override
    protected void onTaskCancelled(FeedFMError error, int attempt) {
        if (error != null) {
            if (attempt < Configuration.MAX_TASK_RETRY_ATTEMPTS) {
                getQueueManager().offerFirst(copy(attempt + 1));
            } else if (mListener != null) {
                mListener.onFail(error);
            }
        }
    }

    @Override
    protected void onTaskFinished(String clientId) {
        if (mListener != null) {
            mListener.onSuccess(clientId);
        }
    }

    @Override
    public PlayerAbstractTask copy(int attempts) {
        PlayerAbstractTask task = new ClientIdTask(getQueueManager(), mWebservice, mListener);
        task.setAttemptCount(attempts);
        return task;
    }

    @Override
    public String toString() {
        return String.format("%s", ClientIdTask.class.getSimpleName());
    }

}
