package io.github.himathahangama.railnet.network;

import io.github.himathahangama.railnet.domain.entity.Railway;
import io.github.himathahangama.railnet.domain.entity.Town;
import io.github.himathahangama.railnet.domain.entity.Warehouse;

import java.util.List;
import java.util.Objects;

/**
 * Maintains referential integrity across towns, railways, and warehouses.
 */
public final class EntityRegistry {
    private final EntityManager<Town> towns = new EntityManager<>();
    private final EntityManager<Railway> railways = new EntityManager<>();
    private final EntityManager<Warehouse> warehouses = new EntityManager<>();

    public void addTown(Town town) {
        towns.addEntity(town);
    }

    public Town findTown(String name) {
        return towns.findEntity(name);
    }

    public boolean containsTown(String name) {
        return towns.containsEntity(name);
    }

    public List<Town> getTowns() {
        return towns.getEntities();
    }

    public void addRailway(Railway railway) {
        Objects.requireNonNull(railway, "railway");
        Town firstTown = railway.getTown1();
        Town secondTown = railway.getTown2();
        if (!containsTown(firstTown.getName()) || !containsTown(secondTown.getName())) {
            throw new IllegalArgumentException(
                    "Both towns must be registered before adding a railway.");
        }

        railways.addEntity(railway);
        firstTown.addRailway(railway);
        secondTown.addRailway(railway);
    }

    public Railway findRailway(String name) {
        return railways.findEntity(name);
    }

    public boolean containsRailway(String name) {
        return railways.containsEntity(name);
    }

    public List<Railway> getRailways() {
        return railways.getEntities();
    }

    public void addWarehouse(String townName, Warehouse warehouse) {
        Objects.requireNonNull(warehouse, "warehouse");
        Town town = findTown(townName);
        if (town == null) {
            throw new IllegalArgumentException("Town " + townName + " is not registered.");
        }
        warehouses.addEntity(warehouse);
        town.addWarehouse(warehouse);
    }

    public boolean containsWarehouse(String name) {
        return warehouses.containsEntity(name);
    }

    public List<Warehouse> getWarehouses() {
        return warehouses.getEntities();
    }
}
