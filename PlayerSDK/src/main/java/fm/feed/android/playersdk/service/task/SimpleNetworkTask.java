package fm.feed.android.playersdk.service.task;

import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
 * Created by mharkins on 9/3/14.
 */
public class SimpleNetworkTask <Response> extends NetworkAbstractTask <Object, Void, Response> {
    public interface SimpleNetworkTaskListener <Response> {
        public void onStart();
        public Response performRequestSynchronous() throws FeedFMError;
        public void onSuccess(Response response);
        public void onFail();
    }

    private SimpleNetworkTaskListener<Response> mListener;

    public SimpleNetworkTask(TaskQueueManager queueManager, Webservice mWebservice, SimpleNetworkTaskListener<Response> listener) {
        super(queueManager, mWebservice);

        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (mListener != null) {
            mListener.onStart();
        }
    }

    @Override
    protected Response doInBackground(Object... params) {
        Response response = null;
        try {
            if (mListener != null) {
                response = mListener.performRequestSynchronous();
            }
        } catch (FeedFMError e) {
            e.printStackTrace();
            cancel(e);
        }

        return response;
    }

    @Override
    protected void onTaskFinished(Response response) {
        if (mListener != null) {
            mListener.onSuccess(response);
        }
    }

    @Override
    protected void onTaskCancelled(FeedFMError error, int attempt) {
        if (error != null && attempt < MAX_TASK_RETRY_ATTEMPTS) {
            getQueueManager().offerFirst(copy(attempt + 1));
        }

        if (error != null && mListener != null) {
            mListener.onFail();
        }
    }

    @Override
    public PlayerAbstractTask copy(int attempts) {
        PlayerAbstractTask task = new SimpleNetworkTask<Response>(getQueueManager(), mWebservice, mListener);
        task.setAttemptCount(attempts);
        return task;
    }


    @Override
    public String toString() {
        return String.format("%s", SimpleNetworkTask.class.getSimpleName());
    }
}
