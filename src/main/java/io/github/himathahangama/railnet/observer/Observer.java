package io.github.himathahangama.railnet.observer;

/**
 * Receives network mutation notifications without depending on concrete reporters.
 */
public interface Observer {
    void update();

    default boolean isPersistent() {
        return false;
    }
}
