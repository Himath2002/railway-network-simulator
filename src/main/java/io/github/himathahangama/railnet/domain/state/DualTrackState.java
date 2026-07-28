package io.github.himathahangama.railnet.domain.state;

import io.github.himathahangama.railnet.domain.entity.Railway;
import io.github.himathahangama.railnet.domain.entity.Town;

import java.util.Objects;

/**
 * Allows standard-capacity freight movement in both directions every day.
 */
public final class DualTrackState implements RailwayState {
    @Override
    public boolean canTransportFrom(Railway railway, Town origin, int day) {
        Objects.requireNonNull(railway, "railway");
        Objects.requireNonNull(origin, "origin");
        railway.requireEndpoint(origin);
        return true;
    }

    @Override
    public int getTransportCapacity(
            Railway railway,
            Town origin,
            Town destination,
            int day) {
        Objects.requireNonNull(destination, "destination");
        if (!railway.getOtherTown(origin).equals(destination)) {
            throw new IllegalArgumentException("Destination is not connected by this railway.");
        }
        return STANDARD_CAPACITY;
    }

    @Override
    public void dayPassed(Railway railway) {
        Objects.requireNonNull(railway, "railway");
    }

    @Override
    public boolean canDuplicate() {
        return false;
    }

    @Override
    public String getDotAttributes() {
        return " [color=\"#1B796B:#1B796B\", label=\"dual\"]";
    }

    @Override
    public RailwayStatus status() {
        return RailwayStatus.DUAL_TRACK;
    }
}
