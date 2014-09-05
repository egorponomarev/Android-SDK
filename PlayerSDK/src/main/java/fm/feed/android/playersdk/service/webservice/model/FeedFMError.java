package fm.feed.android.playersdk.service.webservice.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mharkins on 8/21/14.
 */
public class FeedFMError extends Throwable {
    public static final int CODE_END_OF_PLAYLIST = 9;
    public static final int CODE_NOT_IN_US = 19;
    public static final int CODE_PLAYBACK_ALREADY_STARTED = 20;

    @SerializedName("code") private    int    code;
    @SerializedName("message") private String message;
    @SerializedName("status") private  int    status;

    public FeedFMError(int code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("{code: %d, message: \"%s\", status: %s}", getCode(), getMessage(), getStatus());
    }
}
