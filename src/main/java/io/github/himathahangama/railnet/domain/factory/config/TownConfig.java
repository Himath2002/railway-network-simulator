package io.github.himathahangama.railnet.domain.factory.config;

import io.github.himathahangama.railnet.domain.entity.Town;

public final class TownConfig extends EntityConfig<Town> {
    private final int population;

    public TownConfig(String name, int population) {
        super(name);
        this.population = population;
    }

    public int getPopulation() {
        return population;
    }

    @Override
    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Town name cannot be blank.");
        }
        if (population < 0) {
            throw new IllegalArgumentException("Population cannot be negative.");
        }
    }

    @Override
    public Town create() {
        return new Town(name, population);
    }
}
