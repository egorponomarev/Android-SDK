package fm.feed.android.playersdk.service.webservice.model;

import fm.feed.android.playersdk.service.constant.Configuration;

/**
 * Created by mharkins on 9/10/14.
 */
public class FeedFMConnectivityError extends FeedFMError {
    public FeedFMConnectivityError() {
        super(Configuration.ERROR_CODE_TUNE_NETWORK, "No internet Connection", -1);
    }
}
