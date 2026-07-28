package io.github.himathahangama.railnet.domain.entity;

/**
 * Bounded freight reserve attached to a town.
 */
public final class Warehouse implements NamedEntity {
    private final String name;
    private final int capacity;
    private int stockpile;

    public Warehouse(String name, int capacity) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Warehouse name cannot be blank.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Warehouse capacity must be positive.");
        }
        this.name = name.trim();
        this.capacity = capacity;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getStockpile() {
        return stockpile;
    }

    public int storeGoods(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Stored amount cannot be negative.");
        }
        int accepted = Math.min(amount, capacity - stockpile);
        stockpile += accepted;
        return accepted;
    }

    public int takeGoods(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Requested amount cannot be negative.");
        }
        int removed = Math.min(amount, stockpile);
        stockpile -= removed;
        return removed;
    }
}
