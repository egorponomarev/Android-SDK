package fm.feed.android.playersdk.service.task;

import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
 * Created by mharkins on 9/3/14.
 */
public class SimpleNetworkTask <Response> extends NetworkAbstractTask <Object, Void, Response> {
    public interface SimpleNetworkTaskListener <Response> {
        public Response performRequestSynchronous() throws FeedFMError;
        public void onSuccess(Response response);
        public void onFail();
    }

    private SimpleNetworkTaskListener<Response> mListener;

    public SimpleNetworkTask(TaskQueueManager queueManager, Webservice mWebservice, SimpleNetworkTaskListener<Response> listener) {
        super(queueManager, mWebservice);

        this.mListener = listener;
    }

    @Override
    protected Response doInBackground(Object... params) {
        Response response = null;
        try {
            if (this.mListener != null) {
                response = this.mListener.performRequestSynchronous();
            }
        } catch (FeedFMError e) {
            e.printStackTrace();
            if (this.mListener != null) {
                this.mListener.onFail();
            }
        }

        return response;
    }

    @Override
    protected void onTaskFinished(Response response) {
        if (this.mListener != null) {
            this.mListener.onSuccess(response);
        }
    }

    @Override
    protected void onTaskCancelled() {

    }


    @Override
    public String toString() {
        return String.format("%s", SimpleNetworkTask.class.getSimpleName());
    }
}
