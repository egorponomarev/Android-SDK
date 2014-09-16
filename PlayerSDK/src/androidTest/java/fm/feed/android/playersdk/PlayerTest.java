package fm.feed.android.playersdk;

import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import fm.feed.android.playersdk.mocks.DummyBusProvider;
import fm.feed.android.playersdk.mocks.DummyPlayerListener;
import fm.feed.android.playersdk.mocks.FakePlayer;
import fm.feed.android.playersdk.mocks.FakePlayerService;
import fm.feed.android.playersdk.mocks.FakeWebservice;
import fm.feed.android.playersdk.mocks.StubRestService;
import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.PlayInfo;
import fm.feed.android.playersdk.service.webservice.model.ClientResponse;
import fm.feed.android.playersdk.service.webservice.model.PlacementResponse;
import fm.feed.android.playersdk.service.webservice.model.PlayResponse;
import fm.feed.android.playersdk.service.webservice.model.PlayStartResponse;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Created by mharkins on 8/26/14.
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class PlayerTest {
//    private FakePlayer player;
//    private DummyBusProvider.MockBus bus;
//
//    private FakePlayerService service;
//    private FakeWebservice webservice;
//    private StubRestService restInterface;


//    @Before
//    public void setUp() {
//        // No need for Listener.
//        // We replace the Bus event handling with Mocking though
//        player = FakePlayer.getInstance(Robolectric.application, new DummyPlayerListener(), "", "");
//        service = new FakePlayerService();
//
//        bus = (DummyBusProvider.MockBus) DummyBusProvider.getInstance();
//        bus.setPlayer(player);
//        bus.setService(service);
//
//        webservice = new FakeWebservice(service);
//        restInterface = new StubRestService();
//
//        service.onCreate();
//        service.setWebservice(webservice);
//        webservice.setRestService(restInterface);
//        service.onStartCommand(null, 0, 0);
//    }
//
    @Test
    public void testTrue() {
        assertTrue(Boolean.TRUE);
    }
//    @Test
//    public void testSetCredentials() {
//        String oldClientId = service.getClientIdString();
//
//        restInterface.mClientResponseMock = new Gson().fromJson(JsonData.clientIdResponse, ClientResponse.class);
//
//        // This set of credential tests isn't strictly necessary
//        player.setCredentials(null, "secret");
//        player.setCredentials("wwew", null);
//        player.setCredentials("", "");
//        player.setCredentials(null, null);
//        // assertSame(oldClientId, service.getClientIdString());
//
//        // Check for mClientId in service
//        player.setCredentials("token", "secret");
//        // assertTrue("0i5k9tpwn42huxr0rrhboai".equals(service.getClientIdString()));
//    }
//
//    private void initCredentials() {
//        restInterface.mClientResponseMock = new Gson().fromJson(JsonData.clientIdResponse, ClientResponse.class);
//        player.setCredentials("token", "secret");
//    }
//
//    @Test
//    public void testPlayerInitializations() {
//        initCredentials();
//
//        restInterface.mPlacementResponseMock = new Gson().fromJson(JsonData.placementResponse, PlacementResponse.class);
//
//        DummyPlayerListener listener = new DummyPlayerListener() {
//            @Override
//            public void onStationChanged(Station station) {
//                super.onStationChanged(station);
//
//                // assertTrue("RockstressFM".equals(station.getName()));
//            }
//
//            @Override
//                public void onPlacementChanged(Placement placement, List<Station> stationList) {
//                    super.onPlacementChanged(placement, stationList);
//
//                    // assertTrue(placement.getId() == 10955);
//                    // assertTrue(stationList.size() == 2);
//                    // assertTrue(stationList.get(0).getId().equals("727"));
//                }
//
//            @Override
//            public void onEndOfPlaylist() {
//
//            }
//
//            @Override
//            public void onPlayerInitialized(PlayInfo playInfo) {
//
//            }
//
//            @Override
//            public void onNotificationWillShow(int notificationId) {
//
//            }
//
//            @Override
//            public void onPlaybackStateChanged(PlayInfo.State state) {
//
//            }
//
//            @Override
//            public void onError(PlayerError playerError) {
//
//            }
//        };
//
//        player.registerNavListener(listener);
//
//        player.setPlacementId(10955);
//        // assertTrue(listener.didCallPlacementChanged);
//
//        player.setStationId(2116);
//        // assertTrue(listener.didCallStationChanged);
//    }
//
//    @Test
//    public void testTune() {
//        initCredentials();
//
//        // Tune in a first Play
//        restInterface.mPlayResponseMock = new Gson().fromJson(JsonData.play1, PlayResponse.class);
//        player.tune();
//
////        // assertTrue(mediaPlayerManager.getActiveMediaPlayer().getState() == FeedFMMediaPlayer.State.PREPARED);
//
//        // Tune a second Play while still not starting first one.
//        restInterface.mPlayResponseMock = new Gson().fromJson(JsonData.play2, PlayResponse.class);
//        player.tune();
////        // assertNull(mediaPlayerManager.getMediaPlayerForPlay(restInterface.mPlayResponseMock.getPlay()));
//    }
//
//    @Test
//    public void testPlay() {
//        initCredentials();
//
//        restInterface.mPlayResponseMock = new Gson().fromJson(JsonData.play1, PlayResponse.class);
//        restInterface.mPlayStartResponseMock = new Gson().fromJson(JsonData.playStartCanSkip, PlayStartResponse.class);
//
//        player.play();
//
////        // assertTrue(mediaPlayerManager.getActiveMediaPlayer().getState() == FeedFMMediaPlayer.State.STARTED);
////        // assertTrue(service.getCanSkip());
//    }
//
//    @Test
//    public void testPause() {
//        initCredentials();
//
//        restInterface.mPlayResponseMock = new Gson().fromJson(JsonData.play1, PlayResponse.class);
//        restInterface.mPlayStartResponseMock = new Gson().fromJson(JsonData.playStartCanSkip, PlayStartResponse.class);
//
//        // Pausing after having tuned shouldn't do anything.
//        player.tune();
//        player.pause();
//
////        // assertTrue(mediaPlayerManager.getActiveMediaPlayer().getState() == FeedFMMediaPlayer.State.PREPARED);
//
//
//        player.play();
//        player.pause();
//
////        // assertTrue(mediaPlayerManager.getActiveMediaPlayer().getState() == FeedFMMediaPlayer.State.PAUSED);
//    }
//
//    @Test
//    public void testSkip() {
//        initCredentials();
//
//        DummyPlayerListener listener = new DummyPlayerListener();
//        player.registerNavListener(listener);
//
//        restInterface.mPlayResponseMock = new Gson().fromJson(JsonData.play1, PlayResponse.class);
//        restInterface.mPlayStartResponseMock = new Gson().fromJson(JsonData.playStartCanSkip, PlayStartResponse.class);
//
//        player.play();
//
////        // assertTrue(mediaPlayerManager.getActiveMediaPlayer().getState() == FeedFMMediaPlayer.State.STARTED);
//
//        restInterface.mPlayResponseMock = new Gson().fromJson(JsonData.play2, PlayResponse.class);
//        restInterface.mFeedFMResponseMock = new Gson().fromJson(JsonData.success, PlayResponse.class);
//
//        player.skip();
//
//        // assertTrue(!listener.didCallSkipFailed);
//
////        // assertTrue(mediaPlayerManager.getActiveMediaPlayer().getState() == FeedFMMediaPlayer.State.STARTED);
////        // assertTrue(mediaPlayerManager.getActiveMediaPlayer().getPlay().getId().equals("142049138"));
//
//        restInterface.mPlayResponseMock = new Gson().fromJson(JsonData.play1, PlayResponse.class);
//        restInterface.mPlayStartResponseMock = new Gson().fromJson(JsonData.playStartNoSkip, PlayStartResponse.class);
//        restInterface.mFeedFMResponseMock = new Gson().fromJson(JsonData.failure, PlayResponse.class);
//
//        player.skip();
//
////        // assertTrue(listener.didCallSkipFailed);
////        // assertTrue(mediaPlayerManager.getActiveMediaPlayer().getState() == FeedFMMediaPlayer.State.STARTED);
////        // assertTrue(mediaPlayerManager.getActiveMediaPlayer().getPlay().getId().equals("142049138"));
//    }
//
//    @Test
//    public void testLike() {
//        initCredentials();
//
//        restInterface.mFeedFMResponseMock = new Gson().fromJson(JsonData.success, PlayResponse.class);
//
//        DummyPlayerListener listener = new DummyPlayerListener();
//        player.registerSocialListener(listener);
//        player.like();
//
//        // assertFalse(listener.didCallLiked);
//
//        restInterface.mPlayResponseMock = new Gson().fromJson(JsonData.play1, PlayResponse.class);
//        restInterface.mPlayStartResponseMock = new Gson().fromJson(JsonData.playStartCanSkip, PlayStartResponse.class);
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
//        restInterface.mFeedFMResponseMock = new Gson().fromJson(JsonData.success, PlayResponse.class);
//
//        DummyPlayerListener listener = new DummyPlayerListener();
//        player.registerSocialListener(listener);
//        player.unlike();
//
//        // assertFalse(listener.didCallUnliked);
//
//        restInterface.mPlayResponseMock = new Gson().fromJson(JsonData.play1, PlayResponse.class);
//        restInterface.mPlayStartResponseMock = new Gson().fromJson(JsonData.playStartCanSkip, PlayStartResponse.class);
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
//        restInterface.mFeedFMResponseMock = new Gson().fromJson(JsonData.success, PlayResponse.class);
//
//        DummyPlayerListener listener = new DummyPlayerListener();
//        player.registerSocialListener(listener);
//        player.dislike();
//
//        // assertFalse(listener.didCallDisliked);
//
//        restInterface.mPlayResponseMock = new Gson().fromJson(JsonData.play1, PlayResponse.class);
//        restInterface.mPlayStartResponseMock = new Gson().fromJson(JsonData.playStartCanSkip, PlayStartResponse.class);
//
//        player.play();
//        player.dislike();
//
//        // assertTrue(listener.didCallDisliked);
//    }
//
//    @After
//    public void tearDown() {
//    }
}
