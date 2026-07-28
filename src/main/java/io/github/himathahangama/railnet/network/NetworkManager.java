package io.github.himathahangama.railnet.network;

import io.github.himathahangama.railnet.domain.entity.Railway;
import io.github.himathahangama.railnet.domain.entity.Town;
import io.github.himathahangama.railnet.domain.entity.Warehouse;
import io.github.himathahangama.railnet.domain.factory.EntityFactory;
import io.github.himathahangama.railnet.domain.factory.config.RailwayConfig;
import io.github.himathahangama.railnet.domain.factory.config.TownConfig;
import io.github.himathahangama.railnet.domain.factory.config.WarehouseConfig;
import io.github.himathahangama.railnet.domain.state.SingleTrackUpgradingState;
import io.github.himathahangama.railnet.domain.state.UnderConstructionState;
import io.github.himathahangama.railnet.observer.Observer;
import io.github.himathahangama.railnet.observer.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Application-facing network facade and Observer-pattern subject.
 */
public final class NetworkManager implements Subject {
    public static final int CONSTRUCTION_DAYS = 5;
    public static final int UPGRADE_DAYS = 5;

    private final List<Observer> observers = new ArrayList<>();
    private final EntityRegistry registry = new EntityRegistry();
    private final EntityFactory<Town, TownConfig> townFactory;
    private final EntityFactory<Railway, RailwayConfig> railwayFactory;
    private final EntityFactory<Warehouse, WarehouseConfig> warehouseFactory;

    public NetworkManager(
            EntityFactory<Town, TownConfig> townFactory,
            EntityFactory<Railway, RailwayConfig> railwayFactory,
            EntityFactory<Warehouse, WarehouseConfig> warehouseFactory) {
        this.townFactory = Objects.requireNonNull(townFactory, "townFactory");
        this.railwayFactory = Objects.requireNonNull(railwayFactory, "railwayFactory");
        this.warehouseFactory = Objects.requireNonNull(warehouseFactory, "warehouseFactory");
    }

    @Override
    public void addObserver(Observer observer) {
        Objects.requireNonNull(observer, "observer");
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        List.copyOf(observers).forEach(Observer::update);
    }

    public void cleanupObservers() {
        observers.removeIf(observer -> !observer.isPersistent());
    }

    public List<Town> getTowns() {
        return registry.getTowns();
    }

    public List<Railway> getRailways() {
        return registry.getRailways();
    }

    public List<Warehouse> getWarehouses() {
        return registry.getWarehouses();
    }

    public void addTown(String name, int population) {
        validateName(name, "Town name");
        if (population <= 0) {
            throw new IllegalArgumentException("Town population must be positive.");
        }
        if (registry.containsTown(name)) {
            throw new IllegalArgumentException("Town " + name + " already exists.");
        }

        registry.addTown(townFactory.create(new TownConfig(name, population)));
        notifyObservers();
    }

    public void updateTownPopulation(String name, int newPopulation) {
        Town town = requireTown(name);
        town.setPopulation(newPopulation);
        notifyObservers();
    }

    public void addRailway(String firstTownName, String secondTownName) {
        Town firstTown = requireTown(firstTownName);
        Town secondTown = requireTown(secondTownName);
        if (firstTown.equals(secondTown)) {
            throw new IllegalArgumentException("A railway must connect two different towns.");
        }
        if (findRailway(firstTownName, secondTownName) != null) {
            throw new IllegalArgumentException(
                    "A railway already connects " + firstTownName + " and " + secondTownName + ".");
        }

        String railwayName = firstTownName + "--" + secondTownName;
        Railway railway = railwayFactory.create(new RailwayConfig(
                railwayName,
                firstTown,
                secondTown,
                new UnderConstructionState(CONSTRUCTION_DAYS),
                this::notifyObservers));
        registry.addRailway(railway);
        notifyObservers();
    }

    public void duplicateRailway(String firstTownName, String secondTownName) {
        requireTown(firstTownName);
        requireTown(secondTownName);
        Railway railway = findRailway(firstTownName, secondTownName);
        if (railway == null || !railway.getState().canDuplicate()) {
            throw new IllegalArgumentException(
                    "No eligible single-track railway connects "
                            + firstTownName + " and " + secondTownName + ".");
        }
        railway.setState(new SingleTrackUpgradingState(UPGRADE_DAYS));
    }

    public boolean isRailwayEligibleForDuplication(String firstTownName, String secondTownName) {
        if (!validExistingTown(firstTownName) || !validExistingTown(secondTownName)) {
            return false;
        }
        Railway railway = findRailway(firstTownName, secondTownName);
        return railway != null && railway.getState().canDuplicate();
    }

    public void addWarehouse(String townName, String warehouseName, int capacity) {
        requireTown(townName);
        validateName(warehouseName, "Warehouse name");
        if (capacity <= 0) {
            throw new IllegalArgumentException("Warehouse capacity must be positive.");
        }
        if (registry.containsWarehouse(warehouseName)) {
            throw new IllegalArgumentException(
                    "Warehouse " + warehouseName + " already exists.");
        }

        Warehouse warehouse = warehouseFactory.create(
                new WarehouseConfig(warehouseName, capacity));
        registry.addWarehouse(townName, warehouse);
        notifyObservers();
    }

    public int transportGoodsFromTown(Town town, int requestedAmount) {
        Objects.requireNonNull(town, "town");
        if (requestedAmount < 0) {
            throw new IllegalArgumentException("Requested freight cannot be negative.");
        }

        int fromTown = Math.min(requestedAmount, town.getStockpile());
        town.transportGoods(fromTown);

        int transported = fromTown;
        int remaining = requestedAmount - fromTown;
        for (Warehouse warehouse : town.getWarehouses()) {
            int fromWarehouse = warehouse.takeGoods(remaining);
            transported += fromWarehouse;
            remaining -= fromWarehouse;
            if (remaining == 0) {
                break;
            }
        }
        return transported;
    }

    private Town requireTown(String name) {
        validateName(name, "Town name");
        Town town = registry.findTown(name);
        if (town == null) {
            throw new IllegalArgumentException("Town " + name + " does not exist.");
        }
        return town;
    }

    private boolean validExistingTown(String name) {
        return name != null && !name.isBlank() && registry.containsTown(name);
    }

    private Railway findRailway(String firstTownName, String secondTownName) {
        String directName = firstTownName + "--" + secondTownName;
        if (registry.containsRailway(directName)) {
            return registry.findRailway(directName);
        }
        String reverseName = secondTownName + "--" + firstTownName;
        return registry.containsRailway(reverseName)
                ? registry.findRailway(reverseName)
                : null;
    }

    private void validateName(String name, String field) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank.");
        }
    }
}
