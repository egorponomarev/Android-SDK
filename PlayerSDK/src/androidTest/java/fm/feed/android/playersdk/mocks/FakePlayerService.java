package fm.feed.android.playersdk.mocks;

import android.content.Context;

import com.squareup.otto.Bus;

import org.robolectric.Robolectric;

import fm.feed.android.playersdk.R;
import fm.feed.android.playersdk.service.PlayerService;
import fm.feed.android.playersdk.service.bus.EventMessage;
import fm.feed.android.playersdk.service.util.AudioFocusManager;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.util.ElapsedTimeManager;

/**
 * Created by mharkins on 8/27/14.
 */
public class FakePlayerService extends PlayerService {
    Webservice webservice;

    @Override
    protected void init() {
        super.init();

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
    @Override
    protected void enableForeground() {
        eventBus.post(new EventMessage(EventMessage.Status.NOTIFICATION_WILL_SHOW));
    }

    @Override
    protected void disableForeground() {

    }
}
