package com.feedfm.android.playersdk.service.task;

import com.feedfm.android.playersdk.model.Placement;
import com.feedfm.android.playersdk.service.MediaPlayerManager;
import com.feedfm.android.playersdk.service.TaskQueueManager;
import com.feedfm.android.playersdk.service.webservice.Webservice;

/**
 * Created by mharkins on 9/2/14.
 */
public abstract class MediaPlayerAbstractTask <Params, Progress, Result> extends NetworkAbstractTask <Params, Progress, Result> {
    public MediaPlayerAbstractTask(TaskQueueManager queueManager, Webservice mWebservice) {
        super(queueManager, mWebservice);
    }
}
