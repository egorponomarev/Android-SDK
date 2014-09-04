package fm.feed.android.playersdk.service.task;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import fm.feed.android.playersdk.service.TaskQueueManager;

/**
 * Created by mharkins on 9/2/14.
 */
public abstract class PlayerAbstractTask<Params, Progress, Result> extends AsyncTask <Params, Progress, Result> {
    private TaskQueueManager mQueueManager;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mTaskCancelled = new Runnable() {
        @Override
        public void run() {
            mQueueManager.remove(PlayerAbstractTask.this);
            onTaskCancelled();
            mQueueManager.next();
        }
    };

    protected PlayerAbstractTask(TaskQueueManager queueManager) {
        this.mQueueManager = queueManager;
    }

    @Deprecated
    @Override
    protected void onCancelled() {
        super.onCancelled();

        mHandler.post(mTaskCancelled);
    }

    @Deprecated
    @Override
    protected void onPostExecute(final Result result) {
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


    protected abstract void onTaskCancelled();

    protected abstract void onTaskFinished(Result result);

    @Override
    public String toString() {
        return this.getClass().toString();
    }

    public TaskQueueManager getQueueManager() {
        return mQueueManager;
    }
}
