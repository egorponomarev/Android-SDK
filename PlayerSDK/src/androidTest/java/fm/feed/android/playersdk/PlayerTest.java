package fm.feed.android.playersdk;

import android.content.Intent;

import com.google.gson.Gson;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;

import fm.feed.android.playersdk.mocks.DummyBusProvider;
import fm.feed.android.playersdk.mocks.DummyPlayerListener;
import fm.feed.android.playersdk.mocks.FakePlayer;
import fm.feed.android.playersdk.mocks.FakePlayerService;
import fm.feed.android.playersdk.mocks.FakeRestService;
import fm.feed.android.playersdk.mocks.StubWebservice;
import fm.feed.android.playersdk.service.PlayInfo;
import fm.feed.android.playersdk.service.PlayerService;
import fm.feed.android.playersdk.service.webservice.model.ClientResponse;
import fm.feed.android.playersdk.service.webservice.model.PlacementResponse;
import fm.feed.android.playersdk.service.webservice.model.PlayResponse;
import fm.feed.android.playersdk.service.webservice.model.PlayStartResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Created by mharkins on 8/26/14.
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class PlayerTest {
    private FakePlayer player;
    private DummyBusProvider.MockBus bus;

    private FakePlayerService service;
    private StubWebservice webservice;
    private FakeRestService restInterface;

    private DummyPlayerListener defaultPlayerListener;

    @Before
    public void setUp() {
        bus = (DummyBusProvider.MockBus) DummyBusProvider.getInstance();

        service = new FakePlayerService();
        bus.setService(service);

        webservice = new StubWebservice(service);
        restInterface = new FakeRestService();

        service.onCreate();
        service.setWebservice(webservice);
        webservice.setRestService(restInterface);

        defaultPlayerListener = new DummyPlayerListener();
        // No need for Listener.
        // We replace the Bus event handling with Mocking though
        player = FakePlayer.getInstance(Robolectric.application, bus, defaultPlayerListener, "token", "secret");
        bus.setPlayer(player);

        restInterface.mClientResponseMock = new Gson().fromJson(FakeJsonData.clientIdResponse, ClientResponse.class);
        restInterface.mPlacementResponseMock = new Gson().fromJson(FakeJsonData.defaultPlacementResponse, PlacementResponse.class);

        startPlayer();

        runOneTask(2);
    }

    private void startPlayer() {
        // Start the Service
        Intent intent = new Intent(Robolectric.application, PlayerService.class);
        intent.putExtra(PlayerService.ExtraKeys.timestamp.toString(), new Date().getTime());
        intent.putExtra(PlayerService.ExtraKeys.buildType.toString(), PlayerService.BuildType.RELEASE.name());

        service.onStartCommand(intent, 0, 0);
    }

    /**
     * Wait until the background tasks have ended
     */
    private void runOneTask(int runnableCounts) {

        for (int i = 0; i < runnableCounts; i++) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Robolectric.getUiThreadScheduler().runOneTask();
        }
    }

    @Test
    public void testPlayerInitialization() {
        assertSame("Player should be in READY state", PlayInfo.State.READY, player.getState());

        assertEquals("default placement should be 10955", 10955, player.getPlacement().getId().intValue());
        assertEquals("default station should be 727", 727, player.getStation().getId().intValue());
    }
//
    @Test
    public void testSetCredentials() {
        String oldClientId = service.getClientIdString();

        restInterface.mClientResponseMock = new Gson().fromJson(FakeJsonData.clientIdResponse, ClientResponse.class);

        // This set of credential tests isn't strictly necessary
        player.setCredentials(null, "secret");
        player.setCredentials("token", null);
        player.setCredentials("", "");
        player.setCredentials(null, null);
        assertSame(oldClientId, service.getClientIdString());

        // Check for mClientId in service
        player.setCredentials("token", "secret");

        runOneTask(1);

        assertTrue("0i5k9tpwn42huxr0rrhboai".equals(service.getClientIdString()));
    }

    @Test
    public void testNewPlacement() {
        restInterface.mPlacementResponseMock = new Gson().fromJson(FakeJsonData.otherPlacementResponse, PlacementResponse.class);

        DummyPlayerListener listener = new DummyPlayerListener();

        player.registerNavListener(listener);

        player.setPlacementId(10960);

        runOneTask(1);

        assertTrue(listener.didCallPlacementChanged);
        assertTrue(listener.didCallStationChanged);

        assertEquals("placement ID", 10960, player.getPlacement().getId().intValue());
        assertEquals("station list size", 3, player.getStationList().size());
        assertEquals("station ID", 2290, player.getStation().getId().intValue());
        assertEquals("station name", "Treasure Island Music Festival", player.getStation().getName());

        listener.didCallStationChanged = false;

        player.setStationId(1843);

        runOneTask(1);

        assertTrue(listener.didCallStationChanged);
        assertEquals("station name", "FML Blues Fuzz Radio", player.getStation().getName());
    }

    @Test
    public void testTune() {
        // Tune in a first Play
        restInterface.mPlayResponseMock = new Gson().fromJson(FakeJsonData.play1, PlayResponse.class);

        Robolectric.getUiThreadScheduler().pause();

        player.tune();

        runOneTask(4);

        assertEquals("player state in tuning", PlayInfo.State.TUNED, player.getState());

        // Tune a second Play while still not starting first one.
        restInterface.mPlayResponseMock = new Gson().fromJson(FakeJsonData.play2, PlayResponse.class);
        player.tune();

        runOneTask(2);

        assertNotEquals(null, player.getPlay());
    }

    @Test
    public void testPlay() {
        restInterface.mPlayResponseMock = new Gson().fromJson(FakeJsonData.play1, PlayResponse.class);

        Robolectric.getUiThreadScheduler().pause();
        Robolectric.getBackgroundScheduler().pause();

        player.play();

        runOneTask(6);

        Assert.assertEquals(PlayInfo.State.PLAYING, player.getState());
    }

    @Test
    public void testPlayWhenTuned() {
        restInterface.mPlayResponseMock = new Gson().fromJson(FakeJsonData.play1, PlayResponse.class);
        restInterface.mPlayStartResponseMock = new Gson().fromJson(FakeJsonData.playStartCanSkip, PlayStartResponse.class);

        Robolectric.getUiThreadScheduler().pause();

        player.tune();

        runOneTask(4);

        Assert.assertEquals("play state", PlayInfo.State.TUNED, player.getState());

        player.play();

        runOneTask(2);

        Assert.assertEquals(PlayInfo.State.PLAYING, player.getState());

        runOneTask(4);

        assertTrue("song should be skippable", player.isSkippable());
    }

    @Test
    public void testPause() {
        restInterface.mPlayResponseMock = new Gson().fromJson(FakeJsonData.play1, PlayResponse.class);
        restInterface.mPlayStartResponseMock = new Gson().fromJson(FakeJsonData.playStartCanSkip, PlayStartResponse.class);

        Robolectric.getUiThreadScheduler().pause();

        // Pausing after having tuned shouldn't do anything.
        player.tune();
        runOneTask(4);

        player.pause();

        Assert.assertEquals(PlayInfo.State.TUNED, player.getState());

        player.play();

        runOneTask(2);

        Assert.assertEquals(PlayInfo.State.PLAYING, player.getState());

        player.pause();

        Assert.assertEquals(PlayInfo.State.PAUSED, player.getState());
    }

//    @Test
//    public void testSkip() {
//        DummyPlayerListener listener = new DummyPlayerListener();
//        player.registerNavListener(listener);
//
//        restInterface.mPlayResponseMock = new Gson().fromJson(FakeJsonData.play1, PlayResponse.class);
//        restInterface.mPlayStartResponseMock = new Gson().fromJson(FakeJsonData.playStartCanSkip, PlayStartResponse.class);
//
//        Robolectric.getUiThreadScheduler().pause();
//
//        player.play();
//
//        runOneTask(6);
//
//        Assert.assertEquals(PlayInfo.State.PLAYING, player.getState());
//
//        runOneTask(4);
//
//        assertTrue("song should be skippable", player.isSkippable());
//
//        restInterface.mPlayResponseMock = new Gson().fromJson(FakeJsonData.play2, PlayResponse.class);
//        restInterface.mFeedFMResponseMock = new Gson().fromJson(FakeJsonData.success, FeedFMResponse.class);
//
//        player.skip();
//
//        // Skip Runnables
//        runOneTask(4);
//
//        // Tunning Runnables
//        runOneTask(4);
//
//        Assert.assertEquals(PlayInfo.State.TUNED, player.getState());
////
////        restInterface.mPlayResponseMock = new Gson().fromJson(FakeJsonData.play1, PlayResponse.class);
////        restInterface.mPlayStartResponseMock = new Gson().fromJson(FakeJsonData.playStartNoSkip, PlayStartResponse.class);
////        restInterface.mFeedFMResponseMock = new Gson().fromJson(FakeJsonData.failure, PlayResponse.class);
////
////        player.skip();
////
////        runOneTask(4);
////
////        assertFalse("song should not be skippable", player.isSkippable());
//
////        // assertTrue(listener.didCallSkipFailed);
////        // assertTrue(mediaPlayerManager.getActiveMediaPlayer().getState() == FeedFMMediaPlayer.State.STARTED);
////        // assertTrue(mediaPlayerManager.getActiveMediaPlayer().getPlay().getId().equals("142049138"));
//    }

//    @Test
//    public void testLike() {
//        initCredentials();
//
//        restInterface.mFeedFMResponseMock = new Gson().fromJson(FakeJsonData.success, PlayResponse.class);
//
//        DummyPlayerListener listener = new DummyPlayerListener();
//        player.registerSocialListener(listener);
//        player.like();
//
//        // assertFalse(listener.didCallLiked);
//
//        restInterface.mPlayResponseMock = new Gson().fromJson(FakeJsonData.play1, PlayResponse.class);
//        restInterface.mPlayStartResponseMock = new Gson().fromJson(FakeJsonData.playStartCanSkip, PlayStartResponse.class);
//
//        player.play();
//        player.like();
//
//        // assertTrue(listener.didCallLiked);
//    }
//
//    @Test
//    public void testUnlike() {
//        initCredentials();
//
//        restInterface.mFeedFMResponseMock = new Gson().fromJson(FakeJsonData.success, PlayResponse.class);
//
//        DummyPlayerListener listener = new DummyPlayerListener();
//        player.registerSocialListener(listener);
//        player.unlike();
//
//        // assertFalse(listener.didCallUnliked);
//
//        restInterface.mPlayResponseMock = new Gson().fromJson(FakeJsonData.play1, PlayResponse.class);
//        restInterface.mPlayStartResponseMock = new Gson().fromJson(FakeJsonData.playStartCanSkip, PlayStartResponse.class);
//
//        player.play();
//        player.unlike();
//
//        // assertTrue(listener.didCallUnliked);
//    }
//
//    @Test
//    public void testDislike() {
//        initCredentials();
//
//        restInterface.mFeedFMResponseMock = new Gson().fromJson(FakeJsonData.success, PlayResponse.class);
//
//        DummyPlayerListener listener = new DummyPlayerListener();
//        player.registerSocialListener(listener);
//        player.dislike();
//
//        // assertFalse(listener.didCallDisliked);
//
//        restInterface.mPlayResponseMock = new Gson().fromJson(FakeJsonData.play1, PlayResponse.class);
//        restInterface.mPlayStartResponseMock = new Gson().fromJson(FakeJsonData.playStartCanSkip, PlayStartResponse.class);
//
//        player.play();
//        player.dislike();
//
//        // assertTrue(listener.didCallDisliked);
//    }

    @After
    public void tearDown() {
    }
}
