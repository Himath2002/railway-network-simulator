package io.github.himathahangama.railnet.domain.factory;

import io.github.himathahangama.railnet.domain.entity.Warehouse;
import io.github.himathahangama.railnet.domain.factory.config.WarehouseConfig;

public final class WarehouseFactory implements EntityFactory<Warehouse, WarehouseConfig> {
    @Override
    public Warehouse create(WarehouseConfig config) {
        config.validate();
        return config.create();
    }
}
