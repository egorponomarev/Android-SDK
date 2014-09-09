package fm.feed.android.playersdk.mocks;

import fm.feed.android.playersdk.service.MediaPlayerManager;
import fm.feed.android.playersdk.service.PlayerService;
import fm.feed.android.playersdk.service.webservice.Webservice;
import com.squareup.otto.Bus;

/**
 * Created by mharkins on 8/27/14.
 */
public class FakePlayerService extends PlayerService{
    public void setWebservice(Webservice webservice) {
        mWebservice = webservice;
    }

    public String getClientIdString() {
        return mPlayInfo.getClientId();
    }

    public void setEventBus(Bus bus) {
        eventBus = bus;
    }
}
