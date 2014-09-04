package fm.feed.android.playersdk.service.webservice.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mharkins on 8/25/14.
 */
public class PlayStartResponse extends FeedFMResponse {
    @SerializedName("can_skip") private boolean canSkip;

    public boolean canSkip() {
        return canSkip;
    }

    @Override
    public Object getModel() {
        return canSkip();
    }
}
