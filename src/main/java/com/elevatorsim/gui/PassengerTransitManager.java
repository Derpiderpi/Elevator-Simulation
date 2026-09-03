package com.elevatorsim.gui;

import com.elevatorsim.PassengerTransfer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Spawns and tracks PassengerTransit animations from observed PassengerTransfer events,
// and owns the per-shaft, per-destination pickup-suppression counters that prevent
// double-counting a pickup's passengers as both "resting in the elevator" and "still in
// transit" for their specific destination. Deliberately has no javafx.* imports, so it
// can be unit-tested headlessly.
public class PassengerTransitManager {

    private final List<PassengerTransit> active = new ArrayList<>();
    private final int[][] pickupSuppression; // [shaftIndex][destinationFloor]
    private final int[] lastSeenPickupTick;
    private final int[] lastSeenDropoffTick;

    public PassengerTransitManager(int shaftCount) {
        pickupSuppression = new int[shaftCount][10];
        lastSeenPickupTick = new int[shaftCount];
        lastSeenDropoffTick = new int[shaftCount];
        Arrays.fill(lastSeenPickupTick, -1);
        Arrays.fill(lastSeenDropoffTick, -1);
    }

    // Spawns a new pickup transit for the given shaft if this transfer's tick hasn't
    // already been observed for that shaft. No-op for a null transfer (idle elevator).
    public void observePickup(int shaftIndex, PassengerTransfer pickup, long nowMs) {
        if (pickup == null || pickup.getTick() == lastSeenPickupTick[shaftIndex]) {
            return;
        }
        lastSeenPickupTick[shaftIndex] = pickup.getTick();
        int[] counts = pickup.getCountsByDestination();
        active.add(new PassengerTransit(PassengerTransit.Kind.PICKUP, shaftIndex, pickup.getFloor(),
                pickup.getOriginFloor(), counts, nowMs));
        for (int dest = 0; dest < 10; dest++) {
            pickupSuppression[shaftIndex][dest] += counts[dest];
        }
    }

    // Spawns a new dropoff transit for the given shaft if this transfer's tick hasn't
    // already been observed for that shaft. No-op for a null transfer (idle elevator).
    public void observeDropoff(int shaftIndex, PassengerTransfer dropoff, long nowMs) {
        if (dropoff == null || dropoff.getTick() == lastSeenDropoffTick[shaftIndex]) {
            return;
        }
        lastSeenDropoffTick[shaftIndex] = dropoff.getTick();
        active.add(new PassengerTransit(PassengerTransit.Kind.DROPOFF, shaftIndex, dropoff.getFloor(),
                dropoff.getOriginFloor(), dropoff.getCountsByDestination(), nowMs));
    }

    // Removes completed transits, releasing any pickup suppression they held.
    public void tick(long nowMs) {
        active.removeIf(transit -> {
            if (!transit.isComplete(nowMs)) {
                return false;
            }
            if (transit.getKind() == PassengerTransit.Kind.PICKUP) {
                int[] counts = transit.getCountsByDestination();
                for (int dest = 0; dest < 10; dest++) {
                    pickupSuppression[transit.getShaftIndex()][dest] -= counts[dest];
                }
            }
            return true;
        });
    }

    public int[] getPickupSuppression(int shaftIndex) {
        return pickupSuppression[shaftIndex].clone();
    }

    public List<PassengerTransit> getActiveTransits() {
        return active;
    }
}
