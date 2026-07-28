package io.github.himathahangama.railnet.domain.state;

import io.github.himathahangama.railnet.domain.entity.Railway;
import io.github.himathahangama.railnet.domain.entity.Town;

/**
 * State-pattern contract for railway capacity, lifecycle, and graph rendering.
 */
public interface RailwayState {
    int STANDARD_CAPACITY = 100;

    boolean canTransportFrom(Railway railway, Town origin, int day);

    int getTransportCapacity(Railway railway, Town origin, Town destination, int day);

    void dayPassed(Railway railway);

    boolean canDuplicate();

    String getDotAttributes();

    RailwayStatus status();
}
