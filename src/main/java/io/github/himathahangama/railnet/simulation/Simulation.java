package io.github.himathahangama.railnet.simulation;

import io.github.himathahangama.railnet.domain.entity.Railway;
import io.github.himathahangama.railnet.domain.entity.Town;
import io.github.himathahangama.railnet.input.NetworkEventSource;
import io.github.himathahangama.railnet.network.NetworkManager;
import io.github.himathahangama.railnet.presentation.NetworkReporter;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Coordinates time, events, railway state transitions, freight movement, and reporting.
 */
public final class Simulation {
    private static final Logger LOGGER = Logger.getLogger(Simulation.class.getName());

    private final NetworkEventSource eventSource;
    private final NetworkManager network;
    private final NetworkReporter reporter;
    private final InputStream stopSignal;
    private final Duration tickInterval;

    private int day;

    public Simulation(
            NetworkEventSource eventSource,
            NetworkManager network,
            NetworkReporter reporter) {
        this(eventSource, network, reporter, System.in, Duration.ofSeconds(1));
    }

    public Simulation(
            NetworkEventSource eventSource,
            NetworkManager network,
            NetworkReporter reporter,
            InputStream stopSignal,
            Duration tickInterval) {
        this.eventSource = Objects.requireNonNull(eventSource, "eventSource");
        this.network = Objects.requireNonNull(network, "network");
        this.reporter = Objects.requireNonNull(reporter, "reporter");
        this.stopSignal = Objects.requireNonNull(stopSignal, "stopSignal");
        this.tickInterval = Objects.requireNonNull(tickInterval, "tickInterval");
        if (tickInterval.isNegative()) {
            throw new IllegalArgumentException("Tick interval cannot be negative.");
        }
        network.addObserver(reporter);
    }

    public int currentDay() {
        return day;
    }

    public void run() {
        reporter.printHeader();

        try {
            do {
                waitForNextTick();
                runDay();
            } while (stopSignal.available() == 0);
        } catch (IOException exception) {
            throw new ExternalSimulationException(
                    "Unable to read the stop signal",
                    exception);
        }
    }

    private void runDay() {
        day++;
        reporter.printDayHeader(day);

        List<String> acceptedEvents = processEvents(fetchEvents());
        Map<String, Integer> transportedToday = advanceNetwork();
        reporter.printDailySummary(
                day,
                acceptedEvents,
                network.getTowns(),
                transportedToday);

        if (reporter.isNetworkChanged()) {
            reporter.writeDotFile(network.getTowns(), network.getRailways());
            reporter.resetNetworkChanged();
        }
    }

    private List<String> fetchEvents() {
        Set<String> events = new LinkedHashSet<>();
        String event;
        while ((event = eventSource.nextMessage()) != null) {
            events.add(event);
        }
        return List.copyOf(events);
    }

    private List<String> processEvents(List<String> events) {
        List<String> accepted = new ArrayList<>();
        for (String event : events) {
            reporter.printMessageProcessing(event);
            if (processEvent(event)) {
                accepted.add(event);
            }
        }
        return List.copyOf(accepted);
    }

    private boolean processEvent(String event) {
        String[] parts = event.trim().split("\\s+");
        if (parts.length != 3) {
            reporter.printError("Invalid event format: " + event);
            return false;
        }

        String type = parts[0];
        String firstArgument = parts[1];
        String secondArgument = parts[2];

        try {
            return switch (type) {
                case "town-founding" -> foundTown(
                        firstArgument,
                        parsePositive(secondArgument, "population"));
                case "town-population" -> updatePopulation(
                        firstArgument,
                        parseNonNegative(secondArgument, "population"));
                case "railway-construction" -> constructRailway(
                        firstArgument,
                        secondArgument);
                case "railway-duplication" -> duplicateRailway(
                        firstArgument,
                        secondArgument);
                case "warehouse-founding" -> foundWarehouse(
                        firstArgument,
                        parsePositive(secondArgument, "capacity"));
                default -> {
                    reporter.printError("Unknown event type: " + type);
                    yield false;
                }
            };
        } catch (IllegalArgumentException exception) {
            LOGGER.log(Level.FINE, "Rejected event: " + event, exception);
            reporter.printError(exception.getMessage());
            return false;
        }
    }

    private boolean foundTown(String townName, int population) {
        network.addTown(townName, population);
        network.addWarehouse(
                townName,
                townName + "_Warehouse",
                Math.multiplyExact(population, 10));
        return true;
    }

    private boolean updatePopulation(String townName, int population) {
        network.updateTownPopulation(townName, population);
        return true;
    }

    private boolean constructRailway(String firstTown, String secondTown) {
        network.addRailway(firstTown, secondTown);
        return true;
    }

    private boolean duplicateRailway(String firstTown, String secondTown) {
        network.duplicateRailway(firstTown, secondTown);
        return true;
    }

    private boolean foundWarehouse(String townName, int capacity) {
        network.addWarehouse(
                townName,
                townName + "_Warehouse_" + day,
                capacity);
        return true;
    }

    private Map<String, Integer> advanceNetwork() {
        Map<String, Integer> transportedToday = new HashMap<>();
        for (Town town : network.getTowns()) {
            town.resetTransportedToday();
            town.produceGoods();
            transportedToday.put(town.getName(), 0);
        }

        for (Railway railway : network.getRailways()) {
            moveFreight(railway, railway.getTown1(), railway.getTown2(), transportedToday);
            moveFreight(railway, railway.getTown2(), railway.getTown1(), transportedToday);
        }

        network.getRailways().forEach(Railway::dayPassed);
        network.cleanupObservers();
        return Map.copyOf(transportedToday);
    }

    private void moveFreight(
            Railway railway,
            Town origin,
            Town destination,
            Map<String, Integer> transportedToday) {
        int capacity = railway.getTransportCapacity(origin, destination, day);
        if (capacity == 0) {
            return;
        }

        int amount = network.transportGoodsFromTown(origin, capacity);
        transportedToday.merge(origin.getName(), amount, Integer::sum);
    }

    private int parsePositive(String value, String field) {
        int parsed = parseInteger(value, field);
        if (parsed <= 0) {
            throw new IllegalArgumentException(field + " must be positive: " + value);
        }
        return parsed;
    }

    private int parseNonNegative(String value, String field) {
        int parsed = parseInteger(value, field);
        if (parsed < 0) {
            throw new IllegalArgumentException(field + " cannot be negative: " + value);
        }
        return parsed;
    }

    private int parseInteger(String value, String field) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    field + " must be a whole number: " + value,
                    exception);
        }
    }

    private void waitForNextTick() {
        try {
            Thread.sleep(tickInterval);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ExternalSimulationException(
                    "Simulation interrupted while waiting for the next day",
                    exception);
        }
    }
}
