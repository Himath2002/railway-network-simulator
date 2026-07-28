package io.github.himathahangama.railnet.domain.entity;

import io.github.himathahangama.railnet.domain.state.RailwayStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Network node that produces freight and owns its railway and warehouse relationships.
 */
public final class Town implements NamedEntity {
    private final String name;
    private final List<Railway> railways = new ArrayList<>();
    private final List<Warehouse> warehouses = new ArrayList<>();

    private int population;
    private int stockpile;
    private int transportedToday;

    public Town(String name, int population) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Town name cannot be blank.");
        }
        if (population < 0) {
            throw new IllegalArgumentException("Population cannot be negative.");
        }
        this.name = name.trim();
        this.population = population;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        if (population < 0) {
            throw new IllegalArgumentException("Population cannot be negative.");
        }
        this.population = population;
    }

    public int getStockpile() {
        return stockpile;
    }

    public int getTransportedToday() {
        return transportedToday;
    }

    public List<Railway> getRailways() {
        return List.copyOf(railways);
    }

    public List<Warehouse> getWarehouses() {
        return List.copyOf(warehouses);
    }

    public int getSingleTrackRailways() {
        return (int) railways.stream()
                .filter(railway -> railway.getState().status() == RailwayStatus.SINGLE_TRACK)
                .count();
    }

    public int getDualTrackRailways() {
        return (int) railways.stream()
                .filter(railway -> railway.getState().status() == RailwayStatus.DUAL_TRACK)
                .count();
    }

    public void addRailway(Railway railway) {
        Objects.requireNonNull(railway, "railway");
        railway.requireEndpoint(this);
        if (railways.contains(railway)) {
            throw new IllegalArgumentException(
                    "Railway " + railway.getName() + " is already connected to " + name + ".");
        }
        railways.add(railway);
    }

    public void addWarehouse(Warehouse warehouse) {
        Objects.requireNonNull(warehouse, "warehouse");
        if (warehouses.stream().anyMatch(existing -> existing.getName().equals(warehouse.getName()))) {
            throw new IllegalArgumentException(
                    "Warehouse " + warehouse.getName() + " already exists in " + name + ".");
        }
        warehouses.add(warehouse);
    }

    public void produceGoods() {
        long producedTotal = (long) stockpile + population;
        stockpile = (int) Math.min(producedTotal, Integer.MAX_VALUE);
    }

    public void transportGoods(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Transport amount cannot be negative.");
        }
        if (amount > stockpile) {
            throw new IllegalArgumentException(
                    "Town " + name + " has only " + stockpile + " goods available.");
        }
        stockpile -= amount;
        transportedToday += amount;
    }

    public void resetTransportedToday() {
        transportedToday = 0;
    }
}
