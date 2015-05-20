package fm.feed.android.playersdk.mocks;

import android.content.Context;

import com.squareup.otto.Bus;

import org.robolectric.Robolectric;

import fm.feed.android.playersdk.R;
import fm.feed.android.playersdk.service.PlayerService;
import fm.feed.android.playersdk.service.webservice.Webservice;

/**
 * Created by mharkins on 8/27/14.
 */
public class FakePlayerService extends PlayerService {
    Webservice webservice;

    @Override
    protected void init() {
        super.onFirstStartCommand();

        mWebservice = webservice;
        mPlayInfo = new DummyPlayInfo(getString(R.string.sdk_version));

        mDataPersister = new FakeDataPersister(this);

        mTaskFactory = new FakeTaskFactory();
        mMediaPlayerPool = new DummyMediaPlayerPool();
        mElapsedTimeManager = new StubElapsedTimeManager(mWebservice, mSecondaryQueue);
    }

    @Override
    protected Context getContext() {
        return Robolectric.application;
    }

    public void setWebservice(Webservice webservice) {
        this.webservice = webservice;
    }

    public String getClientIdString() {
        return ((DummyPlayInfo) mPlayInfo).getClientId();
    }

    public void setEventBus(Bus bus) {
        eventBus = bus;
    }

//    @Override
//    protected void initAudioManager() {
//        mAudioFocusManager = new StubAudioFocusManager(this, null);
//    }
//
//    @Override
//    protected void releaseAudioManager() {
//
//    }
//

}
