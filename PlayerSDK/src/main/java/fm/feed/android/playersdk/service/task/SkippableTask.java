package fm.feed.android.playersdk.service.task;

import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.webservice.Webservice;

/**
 * Created by mharkins on 9/11/14.
 */
public abstract class SkippableTask <Params, Progress, Result> extends NetworkAbstractTask <Params, Progress, Result> {
    protected SkippableTask(TaskQueueManager queueManager, Webservice mWebservice) {
        super(queueManager, mWebservice);
    }

    public abstract Play getPlay();

    /**
     * The Task might be set as Skippable, but the server gets the final word.
     *
     * @return {@code true} if a skip candidate, {@code false} otherwise.
     */
    public abstract boolean isSkippableCandidate();

    public abstract Integer getElapsedTime();
}
