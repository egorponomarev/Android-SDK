package fm.feed.android.playersdk;

import fm.feed.android.playersdk.service.constant.ApiErrorEnum;
import fm.feed.android.playersdk.service.constant.PlayerErrorEnum;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
 * Created by mharkins on 9/12/14.
 */
public class PlayerError {
    private int code;
    private String message;

    public PlayerError(FeedFMError feedFMError) {
        this.code = feedFMError.getCode();
        this.message = feedFMError.getMessage();
    }

    protected PlayerError(ApiErrorEnum apiError) {
        this.code = apiError.getCode();
        this.message = apiError.getMessage();
    }

    protected PlayerError(PlayerErrorEnum error) {
        this.code = error.getCode();
        this.message = error.getMessage();
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("{type: \"Generic Error\", code: %d, message: \"%s\"}", getCode(), getMessage());
    }
}
