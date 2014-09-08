package fm.feed.android.playersdk.service.task;

import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.webservice.Webservice;

/**
 * Created by mharkins on 9/2/14.
 */
public abstract class MediaPlayerAbstractTask <Params, Progress, Result> extends NetworkAbstractTask <Params, Progress, Result> {
    public MediaPlayerAbstractTask(TaskQueueManager queueManager, Webservice mWebservice) {
        super(queueManager, mWebservice);
    }
}
