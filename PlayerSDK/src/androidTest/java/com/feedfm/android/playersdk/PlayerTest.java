package com.feedfm.android.playersdk;

import com.feedfm.android.playersdk.mocks.DummyBus;
import com.feedfm.android.playersdk.mocks.DummyPlayerClientListener;
import com.feedfm.android.playersdk.mocks.FakeMediaPlayerManager;
import com.feedfm.android.playersdk.mocks.FakePlayer;
import com.feedfm.android.playersdk.mocks.FakePlayerService;
import com.feedfm.android.playersdk.mocks.FakeWebservice;
import com.feedfm.android.playersdk.mocks.MockPlayerReceiver;
import com.feedfm.android.playersdk.mocks.StubRestService;
import com.feedfm.android.playersdk.model.Placement;
import com.feedfm.android.playersdk.model.Station;
import com.feedfm.android.playersdk.service.FeedFMMediaPlayer;
import com.feedfm.android.playersdk.service.webservice.model.ClientResponse;
import com.feedfm.android.playersdk.service.webservice.model.PlacementResponse;
import com.feedfm.android.playersdk.service.webservice.model.PlayResponse;
import com.feedfm.android.playersdk.service.webservice.model.PlayStartResponse;
import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Created by mharkins on 8/26/14.
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class PlayerTest {
    private FakePlayer player;
    private DummyBus bus;

    private FakePlayerService service;
    private FakeWebservice webservice;
    private StubRestService restInterface;
    private FakeMediaPlayerManager mediaPlayerManager;


    private MockPlayerReceiver playerReceiver;

    @Before
    public void setUp() {
        ShadowLog.stream = System.out;

        playerReceiver = new MockPlayerReceiver();

        // No need for Listener.
        // We replace the Bus event handling with Mocking though
        player = FakePlayer.getInstance(Robolectric.application, new DummyPlayerClientListener());
        service = new FakePlayerService();

        bus = new DummyBus(player, service);

        webservice = new FakeWebservice(service);
        restInterface = new StubRestService();

        mediaPlayerManager = new FakeMediaPlayerManager(service, service);


        service.onCreate();
        service.setWebservice(webservice);
        service.setMediaPlayerManager(mediaPlayerManager);
        webservice.setRestService(restInterface);
        service.onStartCommand(null, 0, 0);
    }

    @Test
    public void testSetCredentials() {
        String oldClientId = service.getClientIdString();

        restInterface.mClientResponseMock = new Gson().fromJson(JsonData.clientIdResponse, ClientResponse.class);

        // This set of credential tests isn't strictly necessary
        player.setCredentials(null, "secret");
        player.setCredentials("wwew", null);
        player.setCredentials("", "");
        player.setCredentials(null, null);
        assertSame(oldClientId, service.getClientIdString());

        // Check for mClientId in service
        player.setCredentials("token", "secret");
        assertTrue("0i5k9tpwn42huxr0rrhboai".equals(service.getClientIdString()));
    }

    private void initCredentials() {
        restInterface.mClientResponseMock = new Gson().fromJson(JsonData.clientIdResponse, ClientResponse.class);
        player.setCredentials("token", "secret");
    }

    @Test
    public void testPlayerInitializations() {
        initCredentials();


        restInterface.mPlacementResponseMock = new Gson().fromJson(JsonData.placementResponse, PlacementResponse.class);

        DummyPlayerClientListener clientListener = new DummyPlayerClientListener() {
            @Override
            public void onStationChanged(Station station) {
                super.onStationChanged(station);

                assertTrue("RockstressFM".equals(station.getName()));
            }

            @Override
                public void onPlacementChanged(Placement placement, List<Station> stationList) {
                    super.onPlacementChanged(placement, stationList);

                    assertTrue(placement.getId() == 10955);
                    assertTrue(stationList.size() == 2);
                    assertTrue(stationList.get(0).getId().equals("727"));
                }
        };

        player.setPlayerListener(clientListener);

        player.setPlacementId(10955);
        assertTrue(clientListener.didCallPlacementChanged);

        player.setStationId("2116");
        assertTrue(clientListener.didCallStationChanged);
    }

    @Test
    public void testTune() {
        initCredentials();

        // Tune in a first Play
        restInterface.mPlayResponseMock = new Gson().fromJson(JsonData.play1, PlayResponse.class);
        player.tune();

        assertTrue(mediaPlayerManager.getActiveMediaPlayer().getState() == FeedFMMediaPlayer.State.PREPARED);

        // Tune a second Play while still not starting first one.
        restInterface.mPlayResponseMock = new Gson().fromJson(JsonData.play2, PlayResponse.class);
        player.tune();
        assertNull(mediaPlayerManager.getMediaPlayerForPlay(restInterface.mPlayResponseMock.getPlay()));
    }

    @Test
    public void testPlay() {
        initCredentials();

        restInterface.mPlayResponseMock = new Gson().fromJson(JsonData.play1, PlayResponse.class);
        restInterface.mPlayStartResponseMock = new Gson().fromJson(JsonData.playStartCanSkip, PlayStartResponse.class);

        player.play();

        assertTrue(mediaPlayerManager.getActiveMediaPlayer().getState() == FeedFMMediaPlayer.State.STARTED);
        assertTrue(service.getCanSkip());
    }

    @Test
    public void testPause() {
        initCredentials();

        restInterface.mPlayResponseMock = new Gson().fromJson(JsonData.play1, PlayResponse.class);
        restInterface.mPlayStartResponseMock = new Gson().fromJson(JsonData.playStartCanSkip, PlayStartResponse.class);

        // Pausing after having tuned shouldn't do anything.
        player.tune();
        player.pause();

        assertTrue(mediaPlayerManager.getActiveMediaPlayer().getState() == FeedFMMediaPlayer.State.PREPARED);


        player.play();
        player.pause();

        assertTrue(mediaPlayerManager.getActiveMediaPlayer().getState() == FeedFMMediaPlayer.State.PAUSED);
    }

    @Test
    public void testSkip() {
        initCredentials();

        DummyPlayerClientListener clientListener = new DummyPlayerClientListener() {
            @Override
            public void onSkipFailed() {
                super.onSkipFailed();
            }
        };
        player.setPlayerListener(clientListener);

        restInterface.mPlayResponseMock = new Gson().fromJson(JsonData.play1, PlayResponse.class);
        restInterface.mPlayStartResponseMock = new Gson().fromJson(JsonData.playStartCanSkip, PlayStartResponse.class);

        player.play();

        assertTrue(mediaPlayerManager.getActiveMediaPlayer().getState() == FeedFMMediaPlayer.State.STARTED);

        restInterface.mPlayResponseMock = new Gson().fromJson(JsonData.play2, PlayResponse.class);
        restInterface.mFeedFMResponseMock = new Gson().fromJson(JsonData.success, PlayResponse.class);

        player.skip();

        assertTrue(!clientListener.didCallSkipFailed);

        assertTrue(mediaPlayerManager.getActiveMediaPlayer().getState() == FeedFMMediaPlayer.State.STARTED);
        assertTrue(mediaPlayerManager.getActiveMediaPlayer().getPlay().getId().equals("142049138"));

        restInterface.mPlayResponseMock = new Gson().fromJson(JsonData.play1, PlayResponse.class);
        restInterface.mPlayStartResponseMock = new Gson().fromJson(JsonData.playStartNoSkip, PlayStartResponse.class);
        restInterface.mFeedFMResponseMock = new Gson().fromJson(JsonData.failure, PlayResponse.class);

        player.skip();

        assertTrue(clientListener.didCallSkipFailed);
        assertTrue(mediaPlayerManager.getActiveMediaPlayer().getState() == FeedFMMediaPlayer.State.STARTED);
        assertTrue(mediaPlayerManager.getActiveMediaPlayer().getPlay().getId().equals("142049138"));
    }

    @Test
    public void testLike() {
        player.like();
        assertTrue(playerReceiver.postedLike);
    }

    @Test
    public void testUnlike() {
        player.unlike();
        assertTrue(playerReceiver.postedUnlike);
    }

    @Test
    public void testDislike() {
        player.dislike();
        assertTrue(playerReceiver.postedDislike);
    }

    @After
    public void tearDown() {
    }
}
