package com.elevatorsim;

// Immutable record of a single pickup or dropoff event at a floor, for GUI polling.
public final class PassengerTransfer {
    private final int floor;
    private final int count;
    private final int tick;

    public PassengerTransfer(int floor, int count, int tick) {
        this.floor = floor;
        this.count = count;
        this.tick = tick;
    }

    public int getFloor() {
        return floor;
    }

    public int getCount() {
        return count;
    }

    public int getTick() {
        return tick;
    }
}
