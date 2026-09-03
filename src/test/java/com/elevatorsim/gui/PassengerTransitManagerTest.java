package com.elevatorsim.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.elevatorsim.PassengerTransfer;
import org.junit.jupiter.api.Test;

class PassengerTransitManagerTest {

    @Test
    void observePickupSpawnsExactlyOneTransitPerTick() {
        PassengerTransitManager manager = new PassengerTransitManager(3);
        PassengerTransfer transfer = new PassengerTransfer(2, 3, 10);

        manager.observePickup(0, transfer, 1_000L);
        manager.observePickup(0, transfer, 1_010L); // same tick, next frame: must not re-spawn
        manager.observePickup(0, transfer, 1_020L);

        assertEquals(1, manager.getActiveTransits().size());
        assertEquals(3, manager.getPickupSuppression(0));
    }

    @Test
    void nullTransferIsNoOp() {
        PassengerTransitManager manager = new PassengerTransitManager(3);
        manager.observePickup(0, null, 1_000L);
        manager.observeDropoff(0, null, 1_000L);
        assertEquals(0, manager.getActiveTransits().size());
        assertEquals(0, manager.getPickupSuppression(0));
    }

    @Test
    void pickupSuppressionReleasedOnCompletion() {
        PassengerTransitManager manager = new PassengerTransitManager(3);
        PassengerTransfer transfer = new PassengerTransfer(4, 5, 20);

        manager.observePickup(1, transfer, 1_000L);
        assertEquals(5, manager.getPickupSuppression(1));

        long midway = 1_000L + PassengerTransit.DURATION_MS / 2;
        manager.tick(midway);
        assertEquals(5, manager.getPickupSuppression(1), "suppression should hold until the transit completes");
        assertEquals(1, manager.getActiveTransits().size());

        long afterCompletion = 1_000L + PassengerTransit.DURATION_MS + 1;
        manager.tick(afterCompletion);
        assertEquals(0, manager.getPickupSuppression(1));
        assertEquals(0, manager.getActiveTransits().size());
    }

    @Test
    void dropoffNeverAffectsSuppression() {
        PassengerTransitManager manager = new PassengerTransitManager(3);
        PassengerTransfer dropoff = new PassengerTransfer(6, 4, 30);

        manager.observeDropoff(2, dropoff, 1_000L);
        assertEquals(0, manager.getPickupSuppression(2));
        assertEquals(1, manager.getActiveTransits().size());

        manager.tick(1_000L + PassengerTransit.DURATION_MS + 1);
        assertEquals(0, manager.getPickupSuppression(2));
        assertEquals(0, manager.getActiveTransits().size());
    }

    @Test
    void concurrentTransitsAcrossShaftsAreIndependent() {
        PassengerTransitManager manager = new PassengerTransitManager(3);
        manager.observePickup(0, new PassengerTransfer(1, 2, 5), 1_000L);
        manager.observePickup(1, new PassengerTransfer(3, 4, 5), 1_000L);
        manager.observeDropoff(2, new PassengerTransfer(7, 1, 5), 1_000L);

        assertEquals(3, manager.getActiveTransits().size());
        assertEquals(2, manager.getPickupSuppression(0));
        assertEquals(4, manager.getPickupSuppression(1));
        assertEquals(0, manager.getPickupSuppression(2));

        for (PassengerTransit transit : manager.getActiveTransits()) {
            assertTrue(transit.getShaftIndex() == 0 || transit.getShaftIndex() == 1 || transit.getShaftIndex() == 2);
        }
    }

    @Test
    void newPickupTickAfterCompletionSpawnsAgain() {
        PassengerTransitManager manager = new PassengerTransitManager(1);
        manager.observePickup(0, new PassengerTransfer(0, 2, 10), 1_000L);
        manager.tick(1_000L + PassengerTransit.DURATION_MS + 1);
        assertEquals(0, manager.getActiveTransits().size());

        manager.observePickup(0, new PassengerTransfer(0, 3, 40), 2_000L);
        assertEquals(1, manager.getActiveTransits().size());
        assertEquals(3, manager.getPickupSuppression(0));
    }
}
