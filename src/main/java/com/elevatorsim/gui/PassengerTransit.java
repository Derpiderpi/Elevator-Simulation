package com.elevatorsim.gui;

// A single passenger-transfer animation (a batch of passengers moving together between
// a floor and an elevator car). Deliberately has no javafx.* imports, so it can be
// unit-tested headlessly, and returns a unitless [0,1] progress like ElevatorInterpolator
// rather than pixel coordinates, leaving pixel mapping to the JavaFX-aware view layer.
public final class PassengerTransit {

    // Fixed real-time duration for a transfer animation. GUI-only artistic constant;
    // the simulation's tick-based scheduling has no natural duration for a transfer.
    public static final long DURATION_MS = 350;

    public enum Kind {
        PICKUP,
        DROPOFF
    }

    private final Kind kind;
    private final int shaftIndex;
    private final int floor;
    private final int originFloor;
    private final int[] countsByDestination;
    private final long startMs;

    public PassengerTransit(Kind kind, int shaftIndex, int floor, int originFloor, int[] countsByDestination, long startMs) {
        this.kind = kind;
        this.shaftIndex = shaftIndex;
        this.floor = floor;
        this.originFloor = originFloor;
        this.countsByDestination = countsByDestination.clone();
        this.startMs = startMs;
    }

    public Kind getKind() {
        return kind;
    }

    public int getShaftIndex() {
        return shaftIndex;
    }

    public int getFloor() {
        return floor;
    }

    public int getOriginFloor() {
        return originFloor;
    }

    public int[] getCountsByDestination() {
        return countsByDestination.clone();
    }

    public int getCount() {
        int sum = 0;
        for (int c : countsByDestination) {
            sum += c;
        }
        return sum;
    }

    // Clamped [0,1] progress through the transfer, mirroring ElevatorInterpolator's clamp style.
    public double computeProgress(long nowMs) {
        double progress = (nowMs - startMs) / (double) DURATION_MS;
        return Math.max(0.0, Math.min(1.0, progress));
    }

    public boolean isComplete(long nowMs) {
        return computeProgress(nowMs) >= 1.0;
    }
}
