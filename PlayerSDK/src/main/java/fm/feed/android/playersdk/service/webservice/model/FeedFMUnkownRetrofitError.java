package fm.feed.android.playersdk.service.webservice.model;

import fm.feed.android.playersdk.service.constant.Configuration;

/**
 * Created by mharkins on 9/10/14.
 */
public class FeedFMUnkownRetrofitError extends FeedFMError {
    public FeedFMUnkownRetrofitError() {
        super(Configuration.ERROR_CODE_TUNE_NETWORK, "Retrofit error response is null or can't be parsed", -1);
    }
}
