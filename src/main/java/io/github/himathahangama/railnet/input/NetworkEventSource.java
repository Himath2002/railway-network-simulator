package io.github.himathahangama.railnet.input;

/**
 * Supplies zero or more network events for the current simulation tick.
 */
@FunctionalInterface
public interface NetworkEventSource {
    String nextMessage();
}
