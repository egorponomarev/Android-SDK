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
 * Created by mharkins on 9/3/14.
 */
public class SimpleNetworkTask<Response> extends NetworkAbstractTask<Object, Void, Response> {
    public interface SimpleNetworkTaskListener<Response> {
        public String getTag();

        public void onStart();

        public Response performRequestSynchronous() throws FeedFMError;

        public void onSuccess(Response response);

        public void onFail(FeedFMError error);
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
        if (error != null && attempt < Configuration.MAX_TASK_RETRY_ATTEMPTS) {
            getQueueManager().offerFirst(copy(attempt + 1));
        }

        if (error != null && mListener != null) {
            mListener.onFail(error);
        }
    }

    @Override
    public String getTag() {
        return mListener.getTag();
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
