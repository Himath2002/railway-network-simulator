package io.github.himathahangama.railnet.domain.factory.config;

import io.github.himathahangama.railnet.domain.entity.Railway;
import io.github.himathahangama.railnet.domain.entity.Town;
import io.github.himathahangama.railnet.domain.state.RailwayState;

/**
 * Validated construction data for a railway and its state-change callback.
 */
public final class RailwayConfig extends EntityConfig<Railway> {
    private final Town town1;
    private final Town town2;
    private final RailwayState state;
    private final Runnable changeListener;

    public RailwayConfig(
            String name,
            Town town1,
            Town town2,
            RailwayState state,
            Runnable changeListener) {
        super(name);
        this.town1 = town1;
        this.town2 = town2;
        this.state = state;
        this.changeListener = changeListener;
    }

    public Town getTown1() {
        return town1;
    }

    public Town getTown2() {
        return town2;
    }

    public RailwayState getState() {
        return state;
    }

    @Override
    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Railway name cannot be blank.");
        }
        if (town1 == null || town2 == null || state == null || changeListener == null) {
            throw new IllegalArgumentException(
                    "Railway towns, state, and change listener are required.");
        }
        if (town1.equals(town2)) {
            throw new IllegalArgumentException("A railway must connect two different towns.");
        }
    }

    @Override
    public Railway create() {
        return new Railway(town1, town2, state, changeListener);
    }
}
