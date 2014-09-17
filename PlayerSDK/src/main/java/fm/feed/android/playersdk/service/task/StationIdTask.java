package fm.feed.android.playersdk.service.task;

import java.util.List;

import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.constant.Configuration;
import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Created by mharkins on 9/2/14.
 */
public class StationIdTask extends PlayerAbstractTask<Object, Void, Station> {
    public interface OnStationIdChanged {
        public void onSuccess(Station station);

        public void onFail(FeedFMError error);
    }

    private OnStationIdChanged mListener;

    public Integer mCurrentStationId;
    public Integer mSelectedStationId;
    public List<Station> mStationList;

    public StationIdTask(TaskQueueManager queueManager, OnStationIdChanged mListener, List<Station> stationList, Integer currentStationId, Integer selectedStationId) {
        super(queueManager);
        this.mListener = mListener;
        this.mSelectedStationId = selectedStationId;
        this.mCurrentStationId = currentStationId;
        this.mStationList = stationList;
    }

    @Override
    protected Station doInBackground(Object... params) {
        // If the user selects the same station, do nothing.
        boolean didChangeStation =
                mCurrentStationId == null ||
                        !mCurrentStationId.equals(mSelectedStationId);
        if (!didChangeStation) {
            return null;
        }

        if (mStationList != null) {
            for (Station s : mStationList) {
                if (s.getId().equals(mSelectedStationId)) {
                    return s;
                }
            }
        }
        return null;
    }

    @Override
    protected void onTaskFinished(Station station) {
        if (this.mListener != null) {
            this.mListener.onSuccess(station);
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
    public PlayerAbstractTask copy(int attempts) {
        PlayerAbstractTask task = new StationIdTask(getQueueManager(), mListener, mStationList, mCurrentStationId, mSelectedStationId);
        task.setAttemptCount(attempts);
        return task;
    }

    @Override
    public String toString() {
        return String.format("%s", StationIdTask.class.getSimpleName());
    }
}
