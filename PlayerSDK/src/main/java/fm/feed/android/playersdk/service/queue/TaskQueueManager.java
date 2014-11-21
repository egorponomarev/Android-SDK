package fm.feed.android.playersdk.service.queue;

import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import fm.feed.android.playersdk.service.task.ClientIdTask;
import fm.feed.android.playersdk.service.task.PlacementIdTask;
import fm.feed.android.playersdk.service.task.PlayTask;
import fm.feed.android.playersdk.service.task.PlayerAbstractTask;
import fm.feed.android.playersdk.service.task.SimpleNetworkTask;
import fm.feed.android.playersdk.service.task.StationIdTask;
import fm.feed.android.playersdk.service.task.TuneTask;

/**
 *
 * This TaskQueueManager keeps track of a list of PlayerAbstractTasks, which
 * are all basically AsyncTasks that are communicating with the Feed.fm servers
 * or a MediaPlayer in a secondary thread. After 'offer()'ing a task to the
 * queue, call 'next()' to make sure that the first task in the queue is
 * started up. When that task completes, it calls 'next()' on the queue to
 * make sure subsequent tasks are started up.
 *
 * Each instance of a TQM has its own Executor for the tasks that it runs, so
 * tasks thrown into different queues will be run concurrently. Tasks within
 * a single queue will be run serially.
 *
 * This class assigns a priority to the different tasks that are placed in it,
 * and addition of a higher prioritiy class can cause lower priority tasks to
 * be removed from the queue.
 *
 * Created by mharkins on 9/2/14.
 */
public class TaskQueueManager extends LinkedList<PlayerAbstractTask> {
    private static final String TAG = TaskQueueManager.class.getSimpleName();

    private Map<Class, List<Class>> mPriorityMap = new HashMap<Class, List<Class>>();

    protected Executor mExecutor = Executors.newSingleThreadExecutor();

    private String mIdentifier;

    private boolean mPaused;

    /**
     * <ul>
     * <li> {@link fm.feed.android.playersdk.service.task.ClientIdTask} will cancel every other task. </li>
     * <li> {@link fm.feed.android.playersdk.service.task.PlacementIdTask} will cancel every task but a {@link fm.feed.android.playersdk.service.task.ClientIdTask} </li>
     * </ul>
     */
    public TaskQueueManager(String identifier) {
        this.mIdentifier = identifier;

        List<Class> allClasses = new ArrayList<Class>();
        allClasses.add(ClientIdTask.class);
        allClasses.add(PlacementIdTask.class);
        allClasses.add(PlayTask.class);
        allClasses.add(TuneTask.class);
        allClasses.add(SimpleNetworkTask.class);
        allClasses.add(StationIdTask.class);

        List<Class> allExceptClientId = new ArrayList<Class>();
        allExceptClientId.addAll(copyWithout(allClasses, new Class[]{ClientIdTask.class}));

        mPriorityMap.put(ClientIdTask.class, new ArrayList<Class>());
        mPriorityMap.put(PlacementIdTask.class, allExceptClientId);
        mPriorityMap.put(StationIdTask.class, allExceptClientId);

        mPaused = false;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public void pause() {
        mPaused = true;
    }

    public void unpause() {
        mPaused = false;
    }

    public boolean isPaused() {
        return mPaused;
    }

    public void next() {
        if (isPaused()) {
            return;
        }

        PlayerAbstractTask task = peek();
        if (task != null) {
            switch (task.getState()) {
                case RUNNING:
                    // If the Task Status is RUNNING, check whether it's been finished or canceled.
                    if (task.isCancelled()) {
                        // Remove top queue item and start the next PENDING one
                        poll();
                        next();
                    }
                    break;
                case FINISHED:
                    // Remove top queue item and start the next PENDING one
                    poll();
                    next();
                    break;
                case PENDING:
                    // Start the PENDING task on the appropriate Thread.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        task.executeOnExecutor(mExecutor);
                    } else {
                        task.execute();
                    }
                    break;
            }
        }
    }

    /**
     * Loops through the queue, removes and cancels previous objects that are the same type of {@code playerTask}
     *
     * @param playerTask
     *         {@link fm.feed.android.playersdk.service.task.PlayerAbstractTask}
     *
     * @return {@code true} if the {@code playerTask} was properly offered to the queue.
     */
    public boolean offerUnique(PlayerAbstractTask playerTask) {
        Log.i(TAG, toString() + ":offerUnique...");
        printQueue();


        List<PlayerAbstractTask> tasksToRemove = new ArrayList<PlayerAbstractTask>();
        for (PlayerAbstractTask task : this) {
            if (playerTask.getClass().equals(task.getClass())) {
                tasksToRemove.add(task);
                task.cancel(true);
            }
        }
        removeAll(tasksToRemove);

        boolean resval = offer(playerTask);

        printQueue();
        Log.i(TAG, toString() + ":...offerUnique");

        return resval;
    }

    /**
     * Loops through the queue, only adds the task if no other of the same type is queued up.
     *
     * @param playerTask
     *         {@link fm.feed.android.playersdk.service.task.PlayerAbstractTask}
     *
     * @return {@code true} if the {@code playerTask} was properly offered to the queue.
     */
    public boolean offerIfNotExist(PlayerAbstractTask playerTask) {
        if (hasTaskType(playerTask.getTag())) {
            return false;
        }
        return offer(playerTask);
    }

    /**
     * Loops through the queue, only adds the task if no other of the same type is queued up.
     *
     * @param playerTask
     *         {@link fm.feed.android.playersdk.service.task.PlayerAbstractTask}
     *
     * @return {@code true} if the {@code playerTask} was properly offered to the queue.
     */
    public boolean offerFirstIfNotExist(PlayerAbstractTask playerTask) {
        if (hasTaskType(playerTask.getTag())) {
            return false;
        }
        return offerFirst(playerTask);
    }

    @Override
    public boolean offer(PlayerAbstractTask o) {
        Log.i(TAG, toString() + ":offer...");
        printQueue();

        boolean retval = super.offer(o);
        o.setQueueManager(this);

        Log.i(TAG, toString() + ":...offer");
        printQueue();

        return retval;
    }

    @Override
    public void clear() {
        for (PlayerAbstractTask task : this) {
            task.cancel(true);
        }
        super.clear();
    }

    /**
     * Cancels and removes lower priority tasks from the Queue
     *
     * @param playerTask
     */
    public void clearLowerPriorities(PlayerAbstractTask playerTask) {
        synchronized (this) {
            Log.i(TAG, toString() + ":clearLowerPriorities...");
            printQueue();

            List<PlayerAbstractTask> tasksToRemove = new ArrayList<PlayerAbstractTask>();
            for (PlayerAbstractTask task : this) {
                // A Task can only cancel tasks of lower priority.
                if (isHigherPriority(playerTask, task)) {
                    task.cancel(true);

                    tasksToRemove.add(task);
                }
            }

            tasksToRemove.removeAll(tasksToRemove);
            printQueue();
            Log.i(TAG, toString() + ":...clearLowerPriorities");
        }
    }

    /**
     * Check to see if {@code task1} has a higher priority than {@code task2}
     * {@code task1} is higher priority if {@code task2} is in the mPriorityMap.get(task1) list.
     *
     * @param task1
     * @param task2
     *
     * @return
     */
    public boolean isHigherPriority(PlayerAbstractTask task1, PlayerAbstractTask task2) {
        List<Class> priorityList = mPriorityMap.get(task1.getClass());
        return (priorityList != null && priorityList.contains(task2.getClass()));
    }

    /**
     * Checks in the Queue whether or not it contains an object of type {@code clazz}
     *
     * @param tag
     *         Task identifier.
     *
     * @return
     */
    public boolean hasTaskType(String tag) {
        for (PlayerAbstractTask task : this) {
            if (tag.equals(task.getTag())) {
                return true;
            }
        }
        return false;
    }

    private List<Class> copyWithout(List<Class> source, Class[] except) {
        List<Class> copy = new ArrayList<Class>(source.size() - except.length);
        for (Class sourceClazz : source) {
            boolean excluded = false;
            for (Class clazz : except) {
                if (sourceClazz.equals(clazz)) {
                    excluded = true;
                    break;
                }
            }
            if (!excluded) {
                copy.add(sourceClazz);
            }
        }
        return copy;
    }

    @Override
    public String toString() {
        return getIdentifier();
    }

    public String getQueueListStr() {
        String sep = "-------------";

        StringBuilder sb = new StringBuilder(".\n" + sep + " " + toString() + " " + sep + "\n");
        if (this.isEmpty()) {
            sb.append("(empty queue)\n");
        } else {
            for (PlayerAbstractTask task : this) {
                sb.append(task + "\n");
            }
        }
        sb.append(sep);
        return sb.toString();
    }

    public void printQueue() {
        Log.i(TAG, getQueueListStr());

    }
}
