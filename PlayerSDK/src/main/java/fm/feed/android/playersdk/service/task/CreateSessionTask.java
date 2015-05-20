package fm.feed.android.playersdk.service.task;

import fm.feed.android.playersdk.model.Session;
import fm.feed.android.playersdk.service.constant.Configuration;
import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

public class CreateSessionTask extends NetworkAbstractTask<Object, Void, Session>  {

    public interface OnSessionCreated {
        public void onSuccess(Session session);

        public void onFail(FeedFMError error);
    }

    private OnSessionCreated mListener;

    public CreateSessionTask(TaskQueueManager queueManager, Webservice mWebservice, OnSessionCreated listener) {
        super(queueManager, mWebservice);
        this.mListener = listener;
    }

    @Override
    protected Session doInBackground(Object... params) {
        Session session = null;
        try {
            session = mWebservice.createSession();
        } catch (FeedFMError feedFMError) {
            feedFMError.printStackTrace();
            cancel(feedFMError);
        }
        return session;
    }

    @Override
    protected void onTaskFinished(Session session) {
        if (mListener != null) {
            mListener.onSuccess(session);
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
    public String getTag() {
        return CreateSessionTask.class.getSimpleName();
    }

    @Override
    public PlayerAbstractTask copy(int attempts) {
        PlayerAbstractTask task = new CreateSessionTask(getQueueManager(), mWebservice, mListener);
        task.setAttemptCount(attempts);
        return task;
    }

    @Override
    public String toString() {
        return String.format("%s", CreateSessionTask.class.getSimpleName());
    }

}
