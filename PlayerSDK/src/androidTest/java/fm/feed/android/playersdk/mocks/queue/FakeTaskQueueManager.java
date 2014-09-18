package fm.feed.android.playersdk.mocks.queue;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import org.robolectric.Robolectric;

import java.util.concurrent.Executor;

import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.task.PlayerAbstractTask;

/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * Created by mharkins on 9/16/14.
 */
public class FakeTaskQueueManager extends TaskQueueManager {
    public FakeTaskQueueManager(String identifier) {
        super(identifier);

        // Override executor to make run on main thread.
//        mExecutor = new Executor() {
//            private final Handler mHandler = new Handler(Looper.getMainLooper());
//
//            @Override
//            public void execute(Runnable command) {
//                mHandler.post(command);
//            }
//        };
    }

    @Override
    public void next() {
        if (isPaused()) {
            return;
        }

        PlayerAbstractTask task = peek();
        if (task != null) {
            switch (task.getStatus()) {
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
}
