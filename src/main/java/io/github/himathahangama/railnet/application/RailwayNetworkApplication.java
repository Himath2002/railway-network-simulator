package io.github.himathahangama.railnet.application;

import io.github.himathahangama.railnet.domain.entity.Railway;
import io.github.himathahangama.railnet.domain.entity.Town;
import io.github.himathahangama.railnet.domain.entity.Warehouse;
import io.github.himathahangama.railnet.domain.factory.EntityFactory;
import io.github.himathahangama.railnet.domain.factory.RailwayFactory;
import io.github.himathahangama.railnet.domain.factory.TownFactory;
import io.github.himathahangama.railnet.domain.factory.WarehouseFactory;
import io.github.himathahangama.railnet.domain.factory.config.RailwayConfig;
import io.github.himathahangama.railnet.domain.factory.config.TownConfig;
import io.github.himathahangama.railnet.domain.factory.config.WarehouseConfig;
import io.github.himathahangama.railnet.input.RandomNetworkEventSource;
import io.github.himathahangama.railnet.network.NetworkManager;
import io.github.himathahangama.railnet.presentation.NetworkReporter;
import io.github.himathahangama.railnet.simulation.ExternalSimulationException;
import io.github.himathahangama.railnet.simulation.Simulation;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Composition root for the railway network simulation.
 */
public final class RailwayNetworkApplication {
    private static final Logger LOGGER =
            Logger.getLogger(RailwayNetworkApplication.class.getName());

    private RailwayNetworkApplication() {
    }

    public static void main(String[] args) {
        try {
            EntityFactory<Town, TownConfig> townFactory = new TownFactory();
            EntityFactory<Railway, RailwayConfig> railwayFactory = new RailwayFactory();
            EntityFactory<Warehouse, WarehouseConfig> warehouseFactory =
                    new WarehouseFactory();

            NetworkManager network = new NetworkManager(
                    townFactory,
                    railwayFactory,
                    warehouseFactory);
            NetworkReporter reporter = new NetworkReporter();
            Simulation simulation = new Simulation(
                    new RandomNetworkEventSource(),
                    network,
                    reporter);

            System.out.println("Press Enter to stop after the current simulation day.");
            simulation.run();
            System.out.println("Simulation stopped.");
        } catch (IllegalArgumentException | ExternalSimulationException exception) {
            LOGGER.log(Level.SEVERE, "The simulation could not continue", exception);
            System.err.println("Simulation stopped: " + exception.getMessage());
        }
    }
}
