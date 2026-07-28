package io.github.himathahangama.railnet.domain.factory;

import io.github.himathahangama.railnet.domain.entity.Railway;
import io.github.himathahangama.railnet.domain.factory.config.RailwayConfig;

public final class RailwayFactory implements EntityFactory<Railway, RailwayConfig> {
    @Override
    public Railway create(RailwayConfig config) {
        config.validate();
        return config.create();
    }
}
