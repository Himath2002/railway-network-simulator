package io.github.himathahangama.railnet.domain.state;

import io.github.himathahangama.railnet.domain.entity.Railway;
import io.github.himathahangama.railnet.domain.entity.Town;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RailwayStateTest {
    @Test
    void constructionTransitionsToAlternatingSingleTrack() {
        AtomicInteger notifications = new AtomicInteger();
        Town east = new Town("East", 500);
        Town west = new Town("West", 500);
        Railway railway = new Railway(
                east,
                west,
                new UnderConstructionState(2),
                notifications::incrementAndGet);

        assertEquals(0, railway.getTransportCapacity(east, west, 1));
        railway.dayPassed();
        railway.dayPassed();

        assertEquals(RailwayStatus.SINGLE_TRACK, railway.getState().status());
        assertEquals(100, railway.getTransportCapacity(east, west, 3));
        assertEquals(0, railway.getTransportCapacity(west, east, 3));
        assertEquals(1, notifications.get());
    }

    @Test
    void upgradeKeepsServiceThenActivatesBothDirections() {
        Town east = new Town("East", 500);
        Town west = new Town("West", 500);
        Railway railway = new Railway(
                east,
                west,
                new SingleTrackUpgradingState(2),
                () -> { });

        assertTrue(railway.canTransportFrom(east, 1));
        assertFalse(railway.canTransportFrom(west, 1));

        railway.dayPassed();
        railway.dayPassed();

        assertEquals(RailwayStatus.DUAL_TRACK, railway.getState().status());
        assertEquals(100, railway.getTransportCapacity(east, west, 3));
        assertEquals(100, railway.getTransportCapacity(west, east, 3));
    }
}
