package com.feedfm.android.playersdk;

import com.feedfm.android.playersdk.mocks.DummyPlayerClientListener;
import com.feedfm.android.playersdk.mocks.FakeBus;
import com.feedfm.android.playersdk.mocks.FakePlayer;
import com.feedfm.android.playersdk.mocks.FakePlayerService;
import com.feedfm.android.playersdk.mocks.FakeWebservice;
import com.feedfm.android.playersdk.mocks.MockPlayerReceiver;
import com.feedfm.android.playersdk.mocks.StubRestService;
import com.feedfm.android.playersdk.model.Placement;
import com.feedfm.android.playersdk.model.Station;
import com.feedfm.android.playersdk.service.webservice.model.ClientResponse;
import com.feedfm.android.playersdk.service.webservice.model.PlacementResponse;
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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Created by mharkins on 8/26/14.
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class PlayerTest {
    private FakePlayer player;
    private FakeBus bus;
    private FakePlayerService service;
    private FakeWebservice webservice;
    private StubRestService restInterface;
    private MockPlayerReceiver playerReceiver;

    @Before
    public void setUp() {
        ShadowLog.stream = System.out;

        playerReceiver = new MockPlayerReceiver();

        // No need for Listener.
        // We replace the Bus event handling with Mocking though
        player = FakePlayer.getInstance(Robolectric.application, new DummyPlayerClientListener());
        service = new FakePlayerService();

        bus = new FakeBus(player, service);

        webservice = new FakeWebservice(service);
        restInterface = new StubRestService();


        service.onCreate();
        service.setWebservice(webservice);
        webservice.setRestService(restInterface);
        service.onStartCommand(null, 0, 0);
    }

    @Test
    public void testSetCredentials() {
        String oldClientId = service.getClientIdString();

        String clientResponse = "{\n" +
                "    \"success\": true,\n" +
                "    \"client_id\": \"0i5k9tpwn42huxr0rrhboai\"\n" +
                "}";

        restInterface.mClientResponseMock = new Gson().fromJson(clientResponse, ClientResponse.class);

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
        String clientResponse = "{\n" +
                "    \"success\": true,\n" +
                "    \"client_id\": \"0i5k9tpwn42huxr0rrhboai\"\n" +
                "}";

        restInterface.mClientResponseMock = new Gson().fromJson(clientResponse, ClientResponse.class);
        player.setCredentials("token", "secret");
    }

    @Test
    public void testPlayerInitializations() {
        initCredentials();

        String placementResponse = "{\n" +
                "    \"success\": true,\n" +
                "    \"placement\": {\n" +
                "        \"id\": \"10955\",\n" +
                "        \"name\": \"GrioSDK\"\n" +
                "    },\n" +
                "    \"stations\": [\n" +
                "        {\n" +
                "            \"id\": \"727\",\n" +
                "            \"name\": \"Pretty Lights Music\",\n" +
                "            \"has_thumbnail\": 0,\n" +
                "            \"options\": {}\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"2116\",\n" +
                "            \"name\": \"RockstressFM\",\n" +
                "            \"has_thumbnail\": 0,\n" +
                "            \"options\": {}\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        restInterface.mPlacementResponseMock = new Gson().fromJson(placementResponse, PlacementResponse.class);

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
        player.tune();
        assertTrue(playerReceiver.postedTune);
    }

    @Test
    public void testPlay() {
        player.play();
        assertTrue(playerReceiver.postedPlay);
    }

    @Test
    public void testPause() {
        player.pause();
        assertTrue(playerReceiver.postedPause);
    }

    @Test
    public void testSkip() {
        player.skip();
        assertTrue(playerReceiver.postedSkip);
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
