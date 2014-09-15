package fm.feed.android.playersdk.service.task;

import fm.feed.android.playersdk.service.constant.Configuration;
import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
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
