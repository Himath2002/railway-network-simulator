package io.github.himathahangama.railnet.simulation;

import io.github.himathahangama.railnet.domain.factory.RailwayFactory;
import io.github.himathahangama.railnet.domain.factory.TownFactory;
import io.github.himathahangama.railnet.domain.factory.WarehouseFactory;
import io.github.himathahangama.railnet.domain.state.RailwayStatus;
import io.github.himathahangama.railnet.input.NetworkEventSource;
import io.github.himathahangama.railnet.network.NetworkManager;
import io.github.himathahangama.railnet.presentation.NetworkReporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void executesOneDeterministicDayAcrossAllBoundaries() {
        Queue<String> events = new ArrayDeque<>();
        events.add("town-founding Northport 400");
        events.add("town-founding Southport 500");
        events.add("railway-construction Northport Southport");
        events.add("malformed");
        NetworkEventSource source = events::poll;

        NetworkManager network = new NetworkManager(
                new TownFactory(),
                new RailwayFactory(),
                new WarehouseFactory());
        ByteArrayOutputStream console = new ByteArrayOutputStream();
        Path graph = temporaryDirectory.resolve("network.dot");
        NetworkReporter reporter = new NetworkReporter(
                new PrintStream(console, true, StandardCharsets.UTF_8),
                graph);
        Simulation simulation = new Simulation(
                source,
                network,
                reporter,
                new ByteArrayInputStream(new byte[]{'\n'}),
                Duration.ZERO);

        simulation.run();

        assertEquals(1, simulation.currentDay());
        assertEquals(2, network.getTowns().size());
        assertEquals(2, network.getWarehouses().size());
        assertEquals(RailwayStatus.UNDER_CONSTRUCTION,
                network.getRailways().getFirst().getState().status());
        assertTrue(Files.exists(graph));
        String output = console.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Events accepted: 3"));
        assertTrue(output.contains("Invalid event format"));
    }
}
