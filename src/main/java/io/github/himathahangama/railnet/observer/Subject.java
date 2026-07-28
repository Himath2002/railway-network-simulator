package io.github.himathahangama.railnet.observer;

/**
 * Observable network boundary.
 */
public interface Subject {
    void addObserver(Observer observer);

    void removeObserver(Observer observer);

    void notifyObservers();
}
