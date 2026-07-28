package io.github.himathahangama.railnet.simulation;

public class ExternalSimulationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ExternalSimulationException(String message) {
        super(message);
    }

    public ExternalSimulationException(String message, Throwable cause) {
        super(message, cause);
    }
}
