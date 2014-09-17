package fm.feed.android.playersdk.mocks;

import com.squareup.otto.Bus;

import fm.feed.android.playersdk.service.PlayerService;
import fm.feed.android.playersdk.service.webservice.Webservice;

/**
 * Created by mharkins on 8/27/14.
 */
public class FakePlayerService extends PlayerService {
    public void setWebservice(Webservice webservice) {
        mWebservice = webservice;
    }

    public String getClientIdString() {
        return ""; // todo: implement
    }

    public void setEventBus(Bus bus) {
        eventBus = bus;
    }
}
