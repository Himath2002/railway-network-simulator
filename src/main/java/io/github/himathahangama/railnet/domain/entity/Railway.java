package io.github.himathahangama.railnet.domain.entity;

import io.github.himathahangama.railnet.domain.state.RailwayState;

import java.util.Objects;

/**
 * A bidirectional connection whose behavior is delegated to its current lifecycle state.
 */
public final class Railway implements NamedEntity {
    private final String name;
    private final Town town1;
    private final Town town2;
    private final Runnable changeListener;

    private RailwayState state;

    public Railway(
            Town town1,
            Town town2,
            RailwayState state,
            Runnable changeListener) {
        this.town1 = Objects.requireNonNull(town1, "town1");
        this.town2 = Objects.requireNonNull(town2, "town2");
        if (town1.equals(town2)) {
            throw new IllegalArgumentException("A railway must connect two different towns.");
        }
        this.state = Objects.requireNonNull(state, "state");
        this.changeListener = Objects.requireNonNull(changeListener, "changeListener");
        this.name = town1.getName() + "--" + town2.getName();
    }

    @Override
    public String getName() {
        return name;
    }

    public Town getTown1() {
        return town1;
    }

    public Town getTown2() {
        return town2;
    }

    public Town getOtherTown(Town town) {
        requireEndpoint(town);
        return town.equals(town1) ? town2 : town1;
    }

    public void requireEndpoint(Town town) {
        Objects.requireNonNull(town, "town");
        if (!town.equals(town1) && !town.equals(town2)) {
            throw new IllegalArgumentException(
                    town.getName() + " is not connected by railway " + name + ".");
        }
    }

    public RailwayState getState() {
        return state;
    }

    public void setState(RailwayState state) {
        this.state = Objects.requireNonNull(state, "state");
        changeListener.run();
    }

    public boolean canTransportFrom(Town town, int day) {
        return state.canTransportFrom(this, town, day);
    }

    public int getTransportCapacity(Town origin, Town destination, int day) {
        return state.getTransportCapacity(this, origin, destination, day);
    }

    public void dayPassed() {
        state.dayPassed(this);
    }
}
