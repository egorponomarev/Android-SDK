package fm.feed.android.playersdk.service.webservice.model;

import fm.feed.android.playersdk.service.constant.PlayerErrorEnum;

/**
 * Created by mharkins on 9/10/14.
 */
public class FeedFMConnectivityError extends FeedFMError {
    public FeedFMConnectivityError() {
        super(PlayerErrorEnum.NO_NETWORK);
    }
}
