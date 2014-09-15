package fm.feed.android.playersdk.service.webservice.model;

import fm.feed.android.playersdk.service.constant.PlayerErrorEnum;

/**
 * Created by mharkins on 9/10/14.
 */
public class FeedFMUnkownRetrofitError extends FeedFMError {
    public FeedFMUnkownRetrofitError() {
        super(PlayerErrorEnum.RETROFIT_UNKNOWN);
    }
}
