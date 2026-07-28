package io.github.himathahangama.railnet.domain.state;

import io.github.himathahangama.railnet.domain.entity.Railway;
import io.github.himathahangama.railnet.domain.entity.Town;

import java.util.Objects;

/**
 * Preserves single-track service during duplication, then transitions to dual track.
 */
public final class SingleTrackUpgradingState implements RailwayState {
    private final SingleTrackState activeTrack = new SingleTrackState();
    private int daysRemaining;

    public SingleTrackUpgradingState(int daysRemaining) {
        if (daysRemaining <= 0) {
            throw new IllegalArgumentException("Upgrade duration must be positive.");
        }
        this.daysRemaining = daysRemaining;
    }

    public int daysRemaining() {
        return daysRemaining;
    }

    @Override
    public boolean canTransportFrom(Railway railway, Town origin, int day) {
        return activeTrack.canTransportFrom(railway, origin, day);
    }

    @Override
    public int getTransportCapacity(
            Railway railway,
            Town origin,
            Town destination,
            int day) {
        return activeTrack.getTransportCapacity(railway, origin, destination, day);
    }

    @Override
    public void dayPassed(Railway railway) {
        Objects.requireNonNull(railway, "railway");
        daysRemaining--;
        if (daysRemaining == 0) {
            railway.setState(new DualTrackState());
        }
    }

    @Override
    public boolean canDuplicate() {
        return false;
    }

    @Override
    public String getDotAttributes() {
        return " [style=\"dashed\", color=\"#1B796B:#1B796B\", label=\"upgrading\"]";
    }

    @Override
    public RailwayStatus status() {
        return RailwayStatus.UPGRADING;
    }
}
