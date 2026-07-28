package io.github.himathahangama.railnet.presentation;

import io.github.himathahangama.railnet.domain.entity.Railway;
import io.github.himathahangama.railnet.domain.entity.Town;
import io.github.himathahangama.railnet.observer.Observer;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Renders console summaries and persists the latest network as Graphviz DOT.
 */
public final class NetworkReporter implements Observer {
    public static final Path DEFAULT_GRAPH_PATH =
            Path.of("build", "outputs", "railway-network.dot");

    private static final Logger LOGGER = Logger.getLogger(NetworkReporter.class.getName());

    private final PrintStream output;
    private final Path graphPath;
    private boolean networkChanged;

    public NetworkReporter() {
        this(System.out, DEFAULT_GRAPH_PATH);
    }

    public NetworkReporter(PrintStream output, Path graphPath) {
        this.output = Objects.requireNonNull(output, "output");
        this.graphPath = Objects.requireNonNull(graphPath, "graphPath");
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public void update() {
        networkChanged = true;
    }

    public boolean isNetworkChanged() {
        return networkChanged;
    }

    public void resetNetworkChanged() {
        networkChanged = false;
    }

    public Path graphPath() {
        return graphPath;
    }

    public void printHeader() {
        output.println();
        output.println("┌──────────────────────────────────────────┐");
        output.println("│        Railway Network Simulator         │");
        output.println("│  towns · track states · freight movement │");
        output.println("└──────────────────────────────────────────┘");
    }

    public void printDayHeader(int day) {
        output.printf("%n── Day %,d ─────────────────────────────────%n%n", day);
    }

    public void printMessageProcessing(String message) {
        output.println("event  " + message);
    }

    public void printError(String error) {
        output.println("error  " + error);
    }

    public void printDailySummary(
            int day,
            List<String> messages,
            Iterable<Town> towns,
            Map<String, Integer> transportedToday) {
        Objects.requireNonNull(messages, "messages");
        Objects.requireNonNull(towns, "towns");
        Objects.requireNonNull(transportedToday, "transportedToday");

        output.printf("%nEvents accepted: %d%n", messages.size());
        if (!messages.isEmpty()) {
            messages.forEach(message -> output.println("  • " + message));
        }

        List<Town> townList = toList(towns);
        if (townList.isEmpty()) {
            output.printf("%nNo towns exist after day %,d.%n", day);
            return;
        }

        output.println();
        output.printf(
                "%-26s %10s %7s %7s %12s %12s%n",
                "Town",
                "Population",
                "Single",
                "Dual",
                "Stock",
                "Transported");
        output.println("-".repeat(82));
        for (Town town : townList) {
            output.printf(
                    Locale.ROOT,
                    "%-26s %,10d %,7d %,7d %,12d %,12d%n",
                    town.getName(),
                    town.getPopulation(),
                    town.getSingleTrackRailways(),
                    town.getDualTrackRailways(),
                    town.getStockpile(),
                    transportedToday.getOrDefault(town.getName(), 0));
        }
    }

    public void writeDotFile(Iterable<Town> towns, Iterable<Railway> railways) {
        Objects.requireNonNull(towns, "towns");
        Objects.requireNonNull(railways, "railways");

        try {
            Path parent = graphPath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (var writer = Files.newBufferedWriter(graphPath, StandardCharsets.UTF_8)) {
                writer.write("graph RailwayNetwork {\n");
                writer.write("    graph [rankdir=LR, bgcolor=\"transparent\"]\n");
                writer.write("    node [shape=box, style=\"rounded,filled\", fillcolor=\"#E8F3EF\","
                        + " color=\"#2A6F62\", fontname=\"Helvetica\"]\n");
                writer.write("    edge [color=\"#385D55\", penwidth=1.8]\n\n");

                for (Town town : towns) {
                    writer.write("    " + quote(town.getName()) + " [label="
                            + quote(town.getName() + "\\npop " + town.getPopulation()
                            + " · stock " + town.getStockpile())
                            + "]\n");
                }
                writer.write("\n");
                for (Railway railway : railways) {
                    writer.write("    " + quote(railway.getTown1().getName()) + " -- "
                            + quote(railway.getTown2().getName())
                            + railway.getState().getDotAttributes()
                            + "\n");
                }
                writer.write("}\n");
            }

            output.println();
            output.println("Graph updated: " + graphPath);
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Unable to write " + graphPath, exception);
            printError("Unable to write graph: " + exception.getMessage());
        }
    }

    private List<Town> toList(Iterable<Town> towns) {
        java.util.ArrayList<Town> result = new java.util.ArrayList<>();
        towns.forEach(result::add);
        return List.copyOf(result);
    }

    private String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
