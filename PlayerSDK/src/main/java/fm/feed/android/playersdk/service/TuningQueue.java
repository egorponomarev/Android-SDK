package fm.feed.android.playersdk.service;

import fm.feed.android.playersdk.service.task.PlayerAbstractTask;
import fm.feed.android.playersdk.service.task.TuneTask;

/**
 * Created by mharkins on 9/5/14.
 */
public class TuningQueue extends TaskQueueManager {
    public TuningQueue(String identifier) {
        super(identifier);
    }

    public TuningQueue() {
        super(TuningQueue.class.getSimpleName());
    }

    public boolean isTuning() {
        PlayerAbstractTask task = peek();
        return (task != null && task instanceof TuneTask && !task.isCancelled());
    }
}
