package fm.feed.android.playersdk.service.task;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import fm.feed.android.playersdk.service.queue.TaskQueueManager;
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
public abstract class PlayerAbstractTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    private TaskQueueManager mQueueManager;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mTaskCancelled = new Runnable() {
        @Override
        public void run() {
            mQueueManager.remove(PlayerAbstractTask.this);
            onTaskCancelled(mError, mAttemptCount);
            mQueueManager.next();
        }
    };

    private FeedFMError mError = null;
    private int mAttemptCount = 0;

    protected PlayerAbstractTask(TaskQueueManager queueManager) {
        this.mQueueManager = queueManager;
    }

    public void setQueueManager(TaskQueueManager queueManager) {
        this.mQueueManager = queueManager;
    }

    @Override
    protected void onPreExecute() {
        Log.i(getClass().getSimpleName(), String.format("%s, onPreExecute...", mQueueManager));
        super.onPreExecute();
    }

    @Deprecated
    @Override
    protected void onCancelled() {
        Log.i(getClass().getSimpleName(), String.format("%s, onCancelled...", mQueueManager));

        super.onCancelled();

        mHandler.post(mTaskCancelled);
    }

    @Deprecated
    @Override
    protected void onPostExecute(final Result result) {
        Log.i(getClass().getSimpleName(), String.format("%s, onPostExecute...", mQueueManager));
        super.onPostExecute(result);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mQueueManager.remove(PlayerAbstractTask.this);
                onTaskFinished(result);
                mQueueManager.next();
            }
        });
    }

    protected void cancel(FeedFMError error) {
        mError = error;
        cancel(true);
    }

    protected void setAttemptCount(int attemptCount) {
        mAttemptCount = attemptCount;
    }

    public int getAttemptCount() {
        return mAttemptCount;
    }

    public FeedFMError getError() {
        return mError;
    }

    protected abstract void onTaskCancelled(FeedFMError error, int attempt);

    protected abstract void onTaskFinished(Result result);

    public abstract PlayerAbstractTask copy(int attempts);

    @Override
    public String toString() {
        return this.getClass().toString();
    }

    public TaskQueueManager getQueueManager() {
        return mQueueManager;
    }
}
