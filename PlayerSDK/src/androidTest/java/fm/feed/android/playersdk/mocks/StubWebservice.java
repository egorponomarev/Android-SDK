package fm.feed.android.playersdk.mocks;

import android.content.Context;

import fm.feed.android.playersdk.service.webservice.Webservice;

/**
 * Created by mharkins on 8/27/14.
 */
public class StubWebservice extends Webservice{
    public StubWebservice(Context context) {
        super(context);
    }

    public void setRestService(RestInterface restService) {
        mRestService = restService;
    }

    @Override
    protected String getAuthStr() {
        return "Basic FakeAuth";
    }
}
