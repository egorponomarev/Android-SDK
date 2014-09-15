package fm.feed.android.playersdk.service.task;

import fm.feed.android.playersdk.model.Placement;
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
public class PlacementIdTask extends NetworkAbstractTask<Object, Void, Placement> {
    public interface OnPlacementIdChanged {
        public void onSuccess(Placement placement);

        public void onFail(FeedFMError error);
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
            cancel(feedFMError);
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
    protected void onTaskCancelled(FeedFMError error, int attempt) {
        // If the task was cancelled because of an error.
        if (error != null) {
            if (attempt < Configuration.MAX_TASK_RETRY_ATTEMPTS) {
                getQueueManager().offerFirst(copy(attempt + 1));
            } else if (mListener != null) {
                mListener.onFail(error);
            }
        }
    }

    @Override
    public PlayerAbstractTask copy(int attempts) {
        PlayerAbstractTask task = new PlacementIdTask(getQueueManager(), mWebservice, mListener, mPlacementId);
        task.setAttemptCount(attempts);
        return task;
    }

    @Override
    public String toString() {
        return String.format("%s", PlacementIdTask.class.getSimpleName());
    }
}
