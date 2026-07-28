package io.github.himathahangama.railnet.input;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomNetworkEventSourceTest {
    @Test
    void seedAndClockMakeTheEventStreamRepeatable() {
        AtomicLong firstClock = new AtomicLong(1_000);
        AtomicLong secondClock = new AtomicLong(1_000);
        RandomNetworkEventSource first =
                new RandomNetworkEventSource(42, firstClock::get);
        RandomNetworkEventSource second =
                new RandomNetworkEventSource(42, secondClock::get);
        first.setErrorProbability(0);
        second.setErrorProbability(0);

        firstClock.addAndGet(1_000);
        secondClock.addAndGet(1_000);

        List<String> firstEvents = drain(first);
        List<String> secondEvents = drain(second);

        assertEquals(firstEvents, secondEvents);
        assertTrue(firstEvents.size() >= 2);
        assertTrue(firstEvents.stream().allMatch(event -> event.split("\\s+").length == 3));
    }

    @Test
    void rejectsUnsafeErrorProbabilities() {
        RandomNetworkEventSource source = new RandomNetworkEventSource(1);

        assertThrows(IllegalArgumentException.class, () -> source.setErrorProbability(-0.1));
        assertThrows(IllegalArgumentException.class, () -> source.setErrorProbability(0.6));
    }

    private List<String> drain(NetworkEventSource source) {
        List<String> events = new ArrayList<>();
        String event;
        while ((event = source.nextMessage()) != null) {
            events.add(event);
        }
        return List.copyOf(events);
    }
}
