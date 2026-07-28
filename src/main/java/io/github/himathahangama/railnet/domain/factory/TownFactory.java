package io.github.himathahangama.railnet.domain.factory;

import io.github.himathahangama.railnet.domain.entity.Town;
import io.github.himathahangama.railnet.domain.factory.config.TownConfig;

public final class TownFactory implements EntityFactory<Town, TownConfig> {
    @Override
    public Town create(TownConfig config) {
        config.validate();
        return config.create();
    }
}
