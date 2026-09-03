package com.elevatorsim.gui;

import com.elevatorsim.ElevatorEvent;

// Computes a continuous fractional floor position for an elevator, given the
// simulation's discrete per-tick scheduling. Self-calibrating: rather than
// trusting a single "simulation start" wall-clock timestamp, callers pass the
// wall-clock time of the last *observed* SimClock tick change, so this does
// not depend on any assumption about when the simulation actually started.
// Deliberately has no javafx.* imports, so it can be unit-tested headlessly.
public final class ElevatorInterpolator {

    private ElevatorInterpolator() {
    }

    // Returns the elevator's fractional floor position.
    // activeLeg: the elevator's in-flight move event, or null if idle.
    // currentFloor: the elevator's last-known resting floor (used when idle).
    // currentTick: the simulation's current tick (SimClock.getTime()).
    // lastTickChangeMs: wall-clock time (System.currentTimeMillis()) the caller last observed currentTick change.
    // nowMs: wall-clock time now.
    // rateOfSimMs: real-time milliseconds per simulated tick.
    public static double computeFloorPosition(ElevatorEvent activeLeg, int currentFloor, int currentTick,
            long lastTickChangeMs, long nowMs, int rateOfSimMs) {
        if (activeLeg == null || rateOfSimMs <= 0) {
            return currentFloor;
        }

        int departureTick = activeLeg.getDepartureTick();
        int arrivalTick = activeLeg.getExpectedArrival();
        int totalTicks = arrivalTick - departureTick;
        int originFloor = activeLeg.getOriginFloor();
        int destinationFloor = activeLeg.getDestination();

        if (totalTicks <= 0) {
            return destinationFloor;
        }

        double elapsedTicks = (currentTick - departureTick) + (nowMs - lastTickChangeMs) / (double) rateOfSimMs;
        double progress = elapsedTicks / totalTicks;
        progress = Math.max(0.0, Math.min(1.0, progress));

        return originFloor + (destinationFloor - originFloor) * progress;
    }
}
