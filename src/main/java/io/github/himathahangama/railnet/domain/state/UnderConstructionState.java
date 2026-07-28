package io.github.himathahangama.railnet.domain.state;

import io.github.himathahangama.railnet.domain.entity.Railway;
import io.github.himathahangama.railnet.domain.entity.Town;

import java.util.Objects;

/**
 * Blocks freight while a new line is being built, then activates a single track.
 */
public final class UnderConstructionState implements RailwayState {
    private int daysRemaining;

    public UnderConstructionState(int daysRemaining) {
        if (daysRemaining <= 0) {
            throw new IllegalArgumentException("Construction duration must be positive.");
        }
        this.daysRemaining = daysRemaining;
    }

    public int daysRemaining() {
        return daysRemaining;
    }

    @Override
    public boolean canTransportFrom(Railway railway, Town origin, int day) {
        validateEndpoint(railway, origin);
        return false;
    }

    @Override
    public int getTransportCapacity(
            Railway railway,
            Town origin,
            Town destination,
            int day) {
        validateRoute(railway, origin, destination);
        return 0;
    }

    @Override
    public void dayPassed(Railway railway) {
        Objects.requireNonNull(railway, "railway");
        daysRemaining--;
        if (daysRemaining == 0) {
            railway.setState(new SingleTrackState());
        }
    }

    @Override
    public boolean canDuplicate() {
        return false;
    }

    @Override
    public String getDotAttributes() {
        return " [style=\"dashed\", color=\"#9AA9A4\", label=\"building\"]";
    }

    @Override
    public RailwayStatus status() {
        return RailwayStatus.UNDER_CONSTRUCTION;
    }

    private void validateEndpoint(Railway railway, Town town) {
        Objects.requireNonNull(railway, "railway");
        Objects.requireNonNull(town, "origin");
        railway.requireEndpoint(town);
    }

    private void validateRoute(Railway railway, Town origin, Town destination) {
        validateEndpoint(railway, origin);
        if (!railway.getOtherTown(origin).equals(destination)) {
            throw new IllegalArgumentException("Destination is not connected by this railway.");
        }
    }
}
