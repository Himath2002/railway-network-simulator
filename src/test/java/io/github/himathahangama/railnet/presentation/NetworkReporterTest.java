package io.github.himathahangama.railnet.presentation;

import io.github.himathahangama.railnet.domain.entity.Railway;
import io.github.himathahangama.railnet.domain.entity.Town;
import io.github.himathahangama.railnet.domain.state.DualTrackState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkReporterTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void writesReadableConsoleAndGraphvizViews() throws IOException {
        ByteArrayOutputStream console = new ByteArrayOutputStream();
        Path graph = temporaryDirectory.resolve("network.dot");
        NetworkReporter reporter = new NetworkReporter(
                new PrintStream(console, true, StandardCharsets.UTF_8),
                graph);
        Town north = new Town("North Port", 400);
        Town south = new Town("South Port", 500);
        Railway railway = new Railway(north, south, new DualTrackState(), reporter::update);

        reporter.printHeader();
        reporter.printDailySummary(
                1,
                List.of("town-founding North_Port 400"),
                List.of(north, south),
                Map.of("North Port", 100, "South Port", 50));
        reporter.writeDotFile(List.of(north, south), List.of(railway));

        String output = console.toString(StandardCharsets.UTF_8);
        String dot = Files.readString(graph);
        assertTrue(output.contains("Railway Network Simulator"));
        assertTrue(output.contains("North Port"));
        assertTrue(dot.contains("\"North Port\" -- \"South Port\""));
        assertTrue(dot.contains("label=\"dual\""));
    }
}
