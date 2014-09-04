package fm.feed.android.playersdk.service.task;

import fm.feed.android.playersdk.service.TaskQueueManager;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
 * Created by mharkins on 9/3/14.
 */
public abstract class SimpleNetworkTask <Response> extends NetworkAbstractTask <Object, Void, Response> {
    public SimpleNetworkTask(TaskQueueManager queueManager, Webservice mWebservice) {
        super(queueManager, mWebservice);
    }

    @Override
    protected Response doInBackground(Object... params) {
        Response response = null;
        try {
             response = performRequestSynchronous();
        } catch (FeedFMError e) {
            e.printStackTrace();
        }

        return response;
    }

    @Override
    protected void onTaskFinished(Response response) {
        onDone(response);
    }

    @Override
    protected void onTaskCancelled() {

    }

    public abstract Response performRequestSynchronous() throws FeedFMError;

    public abstract void onDone(Response response);

    @Override
    public String toString() {
        return String.format("%s", SimpleNetworkTask.class.getSimpleName());
    }
}
