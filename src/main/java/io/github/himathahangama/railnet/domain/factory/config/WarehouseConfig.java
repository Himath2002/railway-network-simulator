package io.github.himathahangama.railnet.domain.factory.config;

import io.github.himathahangama.railnet.domain.entity.Warehouse;

public final class WarehouseConfig extends EntityConfig<Warehouse> {
    private final int capacity;

    public WarehouseConfig(String name, int capacity) {
        super(name);
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Warehouse name cannot be blank.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
    }

    @Override
    public Warehouse create() {
        return new Warehouse(name, capacity);
    }
}
