package com.feedfm.android.playersdk.service.task;

import com.feedfm.android.playersdk.service.TaskQueueManager;
import com.feedfm.android.playersdk.service.webservice.Webservice;
import com.feedfm.android.playersdk.service.webservice.model.FeedFMError;

/**
 * Created by mharkins on 9/2/14.
 */
public abstract class ClientIdTask extends NetworkAbstractTask<Object, Void, String> {
    public ClientIdTask(TaskQueueManager queueManager, Webservice mWebservice) {
        super(queueManager, mWebservice);
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
        onClientIdChanged(clientId);
    }

    public abstract void onClientIdChanged(String clientId);

    @Override
    public String toString() {
        return String.format("%s", ClientIdTask.class.getSimpleName());
    }
}
