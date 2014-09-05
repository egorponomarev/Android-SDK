package fm.feed.android.playersdk.service;

import java.util.ArrayList;
import java.util.List;

import fm.feed.android.playersdk.service.task.PlayTask;
import fm.feed.android.playersdk.service.task.PlayerAbstractTask;

/**
 * Created by mharkins on 9/5/14.
 */
public class MainQueue extends TuningQueue {

    public MainQueue() {
        super(MainQueue.class.getSimpleName());
    }

    /**
     * Checks if the task on top of this Queue is a PlayTask and that it is not Cancelled.
     *
     * @return {@code true} if the {@link PlayTask} is not cancelled.
     */
    public boolean isPlayingTask() {
        PlayerAbstractTask task = peek();
        return (task != null && task instanceof PlayTask && !task.isCancelled());
    }

    /**
     * Removes all the PlayTask instances from this queue.
     */
    public void removeAllPlayTasks() {
        printQueue();

        List<PlayerAbstractTask> tasksToRemove = new ArrayList<PlayerAbstractTask>();
        for (PlayerAbstractTask task : this) {
            if (task instanceof PlayTask) {
                task.cancel(true);
                tasksToRemove.add(task);
            }
        }
        this.removeAll(tasksToRemove);

        printQueue();
    }
}
