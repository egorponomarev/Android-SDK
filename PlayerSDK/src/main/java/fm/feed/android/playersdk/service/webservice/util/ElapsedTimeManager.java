package fm.feed.android.playersdk.service.webservice.util;

import android.os.Handler;
import android.os.Looper;

import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.service.constant.Configuration;
import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.task.PlayTask;
import fm.feed.android.playersdk.service.task.SimpleNetworkTask;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
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

    private ElapsedTimeManager(Webservice webservice, TaskQueueManager secondaryQueue) {
        this.mWebservice = webservice;
        this.mSecondaryQueue = secondaryQueue;
    }

    public static ElapsedTimeManager getInstance(Webservice webservice, TaskQueueManager secondaryQueue) {
        if (sInstance == null) {
            sInstance = new ElapsedTimeManager(webservice, secondaryQueue);
        }
        return sInstance;
    }

    private void updateElapsedTimes() {
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
