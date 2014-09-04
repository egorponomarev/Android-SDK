package fm.feed.android.playersdk.service.webservice.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mharkins on 8/21/14.
 */
public abstract class FeedFMResponse {
    @SerializedName("success")
    private boolean success;
    @SerializedName("error")
    private FeedFMError error;

    public FeedFMResponse() {

    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public FeedFMError getError() {
        return error;
    }

    public abstract Object getModel();
}
