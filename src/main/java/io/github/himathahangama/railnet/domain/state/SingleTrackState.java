package io.github.himathahangama.railnet.domain.state;

import io.github.himathahangama.railnet.domain.entity.Railway;
import io.github.himathahangama.railnet.domain.entity.Town;

import java.util.Objects;

/**
 * Alternates the permitted freight direction each day.
 */
public final class SingleTrackState implements RailwayState {
    @Override
    public boolean canTransportFrom(Railway railway, Town origin, int day) {
        Objects.requireNonNull(railway, "railway");
        Objects.requireNonNull(origin, "origin");
        railway.requireEndpoint(origin);

        boolean oddDay = day % 2 != 0;
        return oddDay
                ? origin.equals(railway.getTown1())
                : origin.equals(railway.getTown2());
    }

    @Override
    public int getTransportCapacity(
            Railway railway,
            Town origin,
            Town destination,
            int day) {
        validateDestination(railway, origin, destination);
        return canTransportFrom(railway, origin, day) ? STANDARD_CAPACITY : 0;
    }

    @Override
    public void dayPassed(Railway railway) {
        Objects.requireNonNull(railway, "railway");
    }

    @Override
    public boolean canDuplicate() {
        return true;
    }

    @Override
    public String getDotAttributes() {
        return " [label=\"single\"]";
    }

    @Override
    public RailwayStatus status() {
        return RailwayStatus.SINGLE_TRACK;
    }

    private void validateDestination(Railway railway, Town origin, Town destination) {
        Objects.requireNonNull(destination, "destination");
        if (!railway.getOtherTown(origin).equals(destination)) {
            throw new IllegalArgumentException("Destination is not connected by this railway.");
        }
    }
}
