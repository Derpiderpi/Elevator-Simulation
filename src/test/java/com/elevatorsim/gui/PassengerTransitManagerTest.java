package com.elevatorsim.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.elevatorsim.PassengerTransfer;
import org.junit.jupiter.api.Test;

class PassengerTransitManagerTest {

    private static int[] counts(int destination, int count) {
        int[] counts = new int[10];
        counts[destination] = count;
        return counts;
    }

    private static int[] counts(int dest1, int count1, int dest2, int count2) {
        int[] counts = new int[10];
        counts[dest1] = count1;
        counts[dest2] = count2;
        return counts;
    }

    @Test
    void observePickupSpawnsExactlyOneTransitPerTick() {
        PassengerTransitManager manager = new PassengerTransitManager(3);
        PassengerTransfer transfer = new PassengerTransfer(2, 2, counts(9, 3), 10);

        manager.observePickup(0, transfer, 1_000L);
        manager.observePickup(0, transfer, 1_010L); // same tick, next frame: must not re-spawn
        manager.observePickup(0, transfer, 1_020L);

        assertEquals(1, manager.getActiveTransits().size());
        assertEquals(3, manager.getPickupSuppression(0)[9]);
    }

    @Test
    void nullTransferIsNoOp() {
        PassengerTransitManager manager = new PassengerTransitManager(3);
        manager.observePickup(0, null, 1_000L);
        manager.observeDropoff(0, null, 1_000L);
        assertEquals(0, manager.getActiveTransits().size());
        for (int d = 0; d < 10; d++) {
            assertEquals(0, manager.getPickupSuppression(0)[d]);
        }
    }

    @Test
    void pickupSuppressionReleasedOnCompletion() {
        PassengerTransitManager manager = new PassengerTransitManager(3);
        PassengerTransfer transfer = new PassengerTransfer(4, 4, counts(8, 5), 20);

        manager.observePickup(1, transfer, 1_000L);
        assertEquals(5, manager.getPickupSuppression(1)[8]);

        long midway = 1_000L + PassengerTransit.DURATION_MS / 2;
        manager.tick(midway);
        assertEquals(5, manager.getPickupSuppression(1)[8], "suppression should hold until the transit completes");
        assertEquals(1, manager.getActiveTransits().size());

        long afterCompletion = 1_000L + PassengerTransit.DURATION_MS + 1;
        manager.tick(afterCompletion);
        assertEquals(0, manager.getPickupSuppression(1)[8]);
        assertEquals(0, manager.getActiveTransits().size());
    }

    @Test
    void suppressionIsPerDestinationNotFlat() {
        PassengerTransitManager manager = new PassengerTransitManager(1);
        // Pickup of 3 passengers heading to floor 2 and 4 heading to floor 7, same tick.
        PassengerTransfer transfer = new PassengerTransfer(0, 0, counts(2, 3, 7, 4), 5);
        manager.observePickup(0, transfer, 1_000L);

        int[] suppression = manager.getPickupSuppression(0);
        assertEquals(3, suppression[2]);
        assertEquals(4, suppression[7]);
        for (int d = 0; d < 10; d++) {
            if (d != 2 && d != 7) {
                assertEquals(0, suppression[d], "destination " + d + " should have no suppression");
            }
        }
    }

    @Test
    void dropoffNeverAffectsSuppression() {
        PassengerTransitManager manager = new PassengerTransitManager(3);
        PassengerTransfer dropoff = new PassengerTransfer(6, 1, counts(6, 4), 30);

        manager.observeDropoff(2, dropoff, 1_000L);
        for (int d = 0; d < 10; d++) {
            assertEquals(0, manager.getPickupSuppression(2)[d]);
        }
        assertEquals(1, manager.getActiveTransits().size());

        manager.tick(1_000L + PassengerTransit.DURATION_MS + 1);
        for (int d = 0; d < 10; d++) {
            assertEquals(0, manager.getPickupSuppression(2)[d]);
        }
        assertEquals(0, manager.getActiveTransits().size());
    }

    @Test
    void concurrentTransitsAcrossShaftsAreIndependent() {
        PassengerTransitManager manager = new PassengerTransitManager(3);
        manager.observePickup(0, new PassengerTransfer(1, 1, counts(5, 2), 5), 1_000L);
        manager.observePickup(1, new PassengerTransfer(3, 3, counts(9, 4), 5), 1_000L);
        manager.observeDropoff(2, new PassengerTransfer(7, 2, counts(7, 1), 5), 1_000L);

        assertEquals(3, manager.getActiveTransits().size());
        assertEquals(2, manager.getPickupSuppression(0)[5]);
        assertEquals(4, manager.getPickupSuppression(1)[9]);
        for (int d = 0; d < 10; d++) {
            assertEquals(0, manager.getPickupSuppression(2)[d]);
        }

        for (PassengerTransit transit : manager.getActiveTransits()) {
            assertTrue(transit.getShaftIndex() == 0 || transit.getShaftIndex() == 1 || transit.getShaftIndex() == 2);
        }
    }

    @Test
    void originFloorPropagatesToSpawnedTransits() {
        PassengerTransitManager manager = new PassengerTransitManager(1);
        manager.observePickup(0, new PassengerTransfer(3, 3, counts(8, 2), 5), 1_000L);
        manager.observeDropoff(0, new PassengerTransfer(8, 3, counts(8, 2), 6), 1_000L);

        assertEquals(2, manager.getActiveTransits().size());
        for (PassengerTransit transit : manager.getActiveTransits()) {
            assertEquals(3, transit.getOriginFloor());
        }
    }

    @Test
    void newPickupTickAfterCompletionSpawnsAgain() {
        PassengerTransitManager manager = new PassengerTransitManager(1);
        manager.observePickup(0, new PassengerTransfer(0, 0, counts(4, 2), 10), 1_000L);
        manager.tick(1_000L + PassengerTransit.DURATION_MS + 1);
        assertEquals(0, manager.getActiveTransits().size());

        manager.observePickup(0, new PassengerTransfer(0, 0, counts(6, 3), 40), 2_000L);
        assertEquals(1, manager.getActiveTransits().size());
        assertEquals(3, manager.getPickupSuppression(0)[6]);
    }
}
