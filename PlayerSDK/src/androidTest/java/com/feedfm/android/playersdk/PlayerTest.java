package com.feedfm.android.playersdk;

import com.squareup.otto.Bus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by mharkins on 8/26/14.
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class PlayerTest {
    private Player player;
    private PlayerReceiverMock playerReceiver;

    private Bus eventBus;

    @Before
    public void setup() {
        eventBus = new Bus();
        playerReceiver = new PlayerReceiverMock();
        eventBus.register(playerReceiver);

        // No need for Listener.
        // We replace the Bus event handling with Mocking though
        player = PlayerMock.getInstance(Robolectric.application, null, eventBus);
    }

    @Test
    public void testSetCredentials() {
        player.setCredentials(null, "secret");
        player.setCredentials("wwew", null);
        player.setCredentials("", "");
        player.setCredentials(null, null);
        assertFalse(playerReceiver.postedCredentials);

        player.setCredentials("token", "secret");
        assertTrue(playerReceiver.postedCredentials);
    }
    @Test
    public void testSetPlacementId() {
        player.setPlacementId(1);
        assertTrue(playerReceiver.postedPlacement);
    }
    @Test
    public void testSetStationId() {
        player.setStationId("2");
        assertTrue(playerReceiver.postedStation);
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
        eventBus.unregister(playerReceiver);
    }
}
