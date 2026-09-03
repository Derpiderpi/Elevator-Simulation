package com.elevatorsim.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.elevatorsim.ElevatorEvent;
import org.junit.jupiter.api.Test;

class ElevatorInterpolatorTest {

    private static final double DELTA = 1e-9;

    @Test
    void idleElevatorReturnsCurrentFloor() {
        double position = ElevatorInterpolator.computeFloorPosition(null, 4, 100, 0L, 0L, 100);
        assertEquals(4.0, position, DELTA);
    }

    @Test
    void zeroRateOfSimReturnsCurrentFloorEvenWithActiveLeg() {
        ElevatorEvent leg = new ElevatorEvent(0, 5, 10, 25);
        double position = ElevatorInterpolator.computeFloorPosition(leg, 0, 15, 0L, 0L, 0);
        assertEquals(0.0, position, DELTA);
    }

    @Test
    void midTravelInterpolatesFractionalPosition() {
        // Leg from floor 0 to floor 4, departing tick 10, arriving tick 20 (10 ticks total).
        ElevatorEvent leg = new ElevatorEvent(0, 4, 10, 20);
        // Observed 5 ticks elapsed (currentTick 15), no additional wall-clock drift within the current tick.
        double position = ElevatorInterpolator.computeFloorPosition(leg, 0, 15, 1_000L, 1_000L, 100);
        assertEquals(2.0, position, DELTA);
    }

    @Test
    void midTravelIncludesSubTickWallClockDrift() {
        // Leg from floor 0 to floor 10, departing tick 0, arriving tick 10 (10 ticks total, 100ms/tick).
        ElevatorEvent leg = new ElevatorEvent(0, 10, 0, 10);
        // currentTick observed as 4 at lastTickChangeMs=1000; 50ms have passed since then (half a tick).
        double position = ElevatorInterpolator.computeFloorPosition(leg, 0, 4, 1_000L, 1_050L, 100);
        assertEquals(4.5, position, DELTA);
    }

    @Test
    void departureTickBoundaryReturnsOriginFloor() {
        ElevatorEvent leg = new ElevatorEvent(2, 7, 10, 25);
        double position = ElevatorInterpolator.computeFloorPosition(leg, 2, 10, 5_000L, 5_000L, 100);
        assertEquals(2.0, position, DELTA);
    }

    @Test
    void arrivalTickBoundaryReturnsDestinationFloor() {
        ElevatorEvent leg = new ElevatorEvent(2, 7, 10, 25);
        double position = ElevatorInterpolator.computeFloorPosition(leg, 2, 25, 5_000L, 5_000L, 100);
        assertEquals(7.0, position, DELTA);
    }

    @Test
    void progressClampsToDestinationWhenElapsedExceedsWindow() {
        // currentTick already past expectedArrival (e.g. GUI polled a stale leg reference just after completion).
        ElevatorEvent leg = new ElevatorEvent(0, 3, 0, 5);
        double position = ElevatorInterpolator.computeFloorPosition(leg, 0, 9, 1_000L, 1_000L, 100);
        assertEquals(3.0, position, DELTA);
    }

    @Test
    void progressClampsToOriginWhenElapsedIsNegative() {
        // currentTick observed before departureTick is possible only via stale wall-clock drift; still must not
        // extrapolate past the origin floor.
        ElevatorEvent leg = new ElevatorEvent(2, 6, 10, 20);
        double position = ElevatorInterpolator.computeFloorPosition(leg, 2, 10, 1_000L, 500L, 100);
        assertEquals(2.0, position, DELTA);
    }

    @Test
    void instantaneousArrivalWithZeroTravelWindowReturnsDestination() {
        // departureTick == expectedArrival: degenerate zero-length window (e.g. loading at the same floor).
        ElevatorEvent leg = new ElevatorEvent(3, 3, 10, 10);
        double position = ElevatorInterpolator.computeFloorPosition(leg, 3, 10, 1_000L, 1_000L, 100);
        assertEquals(3.0, position, DELTA);
    }
}
