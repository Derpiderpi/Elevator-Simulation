package com.elevatorsim.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PassengerTransitTest {

    private static final double DELTA = 1e-9;

    private static int[] counts(int destination, int count) {
        int[] counts = new int[10];
        counts[destination] = count;
        return counts;
    }

    @Test
    void progressIsZeroAtStart() {
        PassengerTransit transit = new PassengerTransit(PassengerTransit.Kind.PICKUP, 0, 3, 3, counts(9, 2), 1_000L);
        assertEquals(0.0, transit.computeProgress(1_000L), DELTA);
        assertFalse(transit.isComplete(1_000L));
    }

    @Test
    void progressIsHalfwayAtHalfDuration() {
        PassengerTransit transit = new PassengerTransit(PassengerTransit.Kind.PICKUP, 1, 5, 5, counts(0, 3), 1_000L);
        long halfway = 1_000L + PassengerTransit.DURATION_MS / 2;
        assertEquals(0.5, transit.computeProgress(halfway), DELTA);
        assertFalse(transit.isComplete(halfway));
    }

    @Test
    void progressIsOneAtExactDuration() {
        PassengerTransit transit = new PassengerTransit(PassengerTransit.Kind.DROPOFF, 2, 7, 3, counts(7, 1), 1_000L);
        long end = 1_000L + PassengerTransit.DURATION_MS;
        assertEquals(1.0, transit.computeProgress(end), DELTA);
        assertTrue(transit.isComplete(end));
    }

    @Test
    void progressClampsToOneAfterDuration() {
        PassengerTransit transit = new PassengerTransit(PassengerTransit.Kind.DROPOFF, 0, 0, 5, counts(0, 4), 1_000L);
        long wayAfter = 1_000L + PassengerTransit.DURATION_MS * 10;
        assertEquals(1.0, transit.computeProgress(wayAfter), DELTA);
        assertTrue(transit.isComplete(wayAfter));
    }

    @Test
    void progressClampsToZeroBeforeStart() {
        PassengerTransit transit = new PassengerTransit(PassengerTransit.Kind.PICKUP, 0, 0, 0, counts(3, 1), 1_000L);
        assertEquals(0.0, transit.computeProgress(500L), DELTA);
        assertFalse(transit.isComplete(500L));
    }

    @Test
    void gettersReturnConstructedValues() {
        int[] byDestination = counts(9, 5);
        PassengerTransit transit = new PassengerTransit(PassengerTransit.Kind.PICKUP, 2, 6, 6, byDestination, 1_000L);
        assertEquals(PassengerTransit.Kind.PICKUP, transit.getKind());
        assertEquals(2, transit.getShaftIndex());
        assertEquals(6, transit.getFloor());
        assertEquals(6, transit.getOriginFloor());
        assertEquals(5, transit.getCount());
        assertEquals(5, transit.getCountsByDestination()[9]);
    }

    @Test
    void countIsSumAcrossMultipleDestinations() {
        int[] byDestination = new int[10];
        byDestination[2] = 3;
        byDestination[8] = 4;
        PassengerTransit transit = new PassengerTransit(PassengerTransit.Kind.PICKUP, 0, 5, 5, byDestination, 1_000L);
        assertEquals(7, transit.getCount());
    }
}
