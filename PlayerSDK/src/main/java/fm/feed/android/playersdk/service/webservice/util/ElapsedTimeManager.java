package fm.feed.android.playersdk.service.webservice.util;

import android.os.Handler;
import android.os.Looper;

import fm.feed.android.playersdk.service.constant.Configuration;
import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.task.PlayTask;
import fm.feed.android.playersdk.service.task.SimpleNetworkTask;
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
 * Created by mharkins on 9/12/14.
 */
public class ElapsedTimeManager {
    private Webservice mWebservice;
    private TaskQueueManager mSecondaryQueue;

    private Handler mTimingHandler = new Handler(Looper.myLooper());

    private PlayTask mPlayTask;

    private static ElapsedTimeManager sInstance = null;

    /**
     * While playing, elapsed time should be sent every 10 seconds.
     */
    private Runnable mSendElapsedTime = new Runnable() {
        @Override
        public void run() {
            final String playId = mPlayTask.getPlay().getId();
            final Integer elapsedTime = mPlayTask.getElapsedTime();

            SimpleNetworkTask<Boolean> elapsedTask = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice, new SimpleNetworkTask.SimpleNetworkTaskListener<Boolean>() {
                @Override
                public void onStart() {

                }

                @Override
                public Boolean performRequestSynchronous() throws FeedFMError {
                    return mWebservice.elapsed(playId, elapsedTime);
                }

                @Override
                public void onSuccess(Boolean aBoolean) {
                    updateElapsedTimes();
                }

                @Override
                public void onFail(FeedFMError error) {

                }
            });
            mSecondaryQueue.offer(elapsedTask);
            mSecondaryQueue.next();
        }
    };

    protected ElapsedTimeManager(Webservice webservice, TaskQueueManager secondaryQueue) {
        this.mWebservice = webservice;
        this.mSecondaryQueue = secondaryQueue;
    }

    public static ElapsedTimeManager getInstance(Webservice webservice, TaskQueueManager secondaryQueue) {
        if (sInstance == null) {
            sInstance = new ElapsedTimeManager(webservice, secondaryQueue);
        }
        return sInstance;
    }

    protected void updateElapsedTimes() {
        mTimingHandler.postDelayed(mSendElapsedTime, Configuration.ELAPSED_PING_INTERVAL);
    }

    public void start(PlayTask playTask) {
        mPlayTask = playTask;
        updateElapsedTimes();
    }

    public void stop() {
        mTimingHandler.removeCallbacks(mSendElapsedTime);
    }
}
