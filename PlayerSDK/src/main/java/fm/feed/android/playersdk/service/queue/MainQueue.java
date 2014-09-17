package fm.feed.android.playersdk.service.queue;

import java.util.ArrayList;
import java.util.List;

import fm.feed.android.playersdk.service.task.PlayTask;
import fm.feed.android.playersdk.service.task.PlayerAbstractTask;

/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
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
    public boolean hasActivePlayTask() {
        PlayerAbstractTask task = peek();
        return (task != null && task instanceof PlayTask && !task.isCancelled());
    }

    /**
     * Checks if there is a {@link fm.feed.android.playersdk.service.task.PlayTask} queued that is not Cancelled;
     *
     * @return {@code true} if the {@link PlayTask} is not cancelled.
     */
    public boolean hasPlayTask() {
        for (PlayerAbstractTask task : this) {
            if (task instanceof PlayTask && !task.isCancelled()) {
                return true;
            }
        }
        return false;
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
