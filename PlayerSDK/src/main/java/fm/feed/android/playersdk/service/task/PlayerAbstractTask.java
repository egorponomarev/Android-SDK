package fm.feed.android.playersdk.service.task;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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
