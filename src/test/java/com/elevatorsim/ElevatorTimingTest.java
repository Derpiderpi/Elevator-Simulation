package com.elevatorsim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Black-box behavioral test verifying elevators abide by the documented timing rules
// (README "Elevator Rules"): 3 simulated seconds to traverse one floor, 5 simulated
// seconds to load/unload passengers at a stop. Drives a real Elevator + BuildingManager +
// SimClock directly (no ElevatorSimulation/ElevatorConfig.txt involved, so timing is
// deterministic and doesn't depend on real-time Thread.sleep pacing), advancing SimClock
// tick-by-tick from the test thread and polling the elevator's existing public getters for
// state transitions. Does not modify any production source file.
//
// Each scenario uses a single destination per stop (not multiple simultaneous destinations
// loaded together), so Elevator.run()'s per-leg duration formula cleanly reduces to
// distance*3 + 5 for both the pickup leg and the dropoff leg, directly matching the
// documented rule without depending on the multi-destination "5*moveQueue.size()" staggering
// used only when several destinations are loaded in the same pickup.
//
// Polling notes (Elevator.run()'s fields are all volatile, but a transition writes several
// of them in sequence within one loop iteration - the JMM only guarantees that a thread which
// observes the LAST write in that sequence also sees every earlier write in the same thread's
// program order, not the other way round):
//   - "detecting" -> pickup leg created: activeLeg is the only relevant write; safe to poll on.
//   - pickup arrival ("Loading passengers" branch): currentfloor is written FIRST, then
//     passengers are loaded, and activeLeg (the new dropoff leg) is written LAST. Poll on the
//     activeLeg reference changing - by the time that's observed, currentfloor and the
//     passenger count are guaranteed already updated.
//   - dropoff arrival ("Unloading passengers" branch): activeLeg is written FIRST (often to
//     null), passengers are unloaded next, and currentfloor is written LAST. Poll on
//     currentFloor changing - by the time that's observed, activeLeg and the passenger count
//     are guaranteed already updated.
class ElevatorTimingTest {

    private static final long POLL_TIMEOUT_MS = 2000;

    private Thread elevatorThread;

    @BeforeEach
    void resetClock() {
        new SimClock(); // resets the shared static clock to 0
    }

    @AfterEach
    void stopElevatorThread() {
        if (elevatorThread != null) {
            elevatorThread.interrupt();
        }
    }

    private Elevator startElevator(BuildingManager manager) {
        Elevator elevator = new Elevator(0, manager);
        elevatorThread = new Thread(elevator, "test-elevator");
        elevatorThread.setDaemon(true);
        elevatorThread.start();
        return elevator;
    }

    private static void awaitTrue(BooleanSupplier condition, String description) {
        long deadline = System.currentTimeMillis() + POLL_TIMEOUT_MS;
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() > deadline) {
                fail("Timed out waiting for: " + description);
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Interrupted while waiting for: " + description);
            }
        }
    }

    private static void advanceClockTo(int targetTick) {
        while (SimClock.getTime() < targetTick) {
            SimClock.tick();
        }
    }

    @Test
    void goingUpPickupAndDropoffTakeExactlyThreeSecondsPerFloorPlusFiveSecondsPerStop() {
        BuildingManager manager = new BuildingManager();
        Elevator elevator = startElevator(manager);

        // 2 passengers waiting at floor 5, heading to floor 8 (going up).
        manager.updatePassengerRequest(5, 8, 2);

        // Pickup leg: elevator starts idle at floor 0, travels 5 floors to floor 5, then loads.
        // Expected duration = 5 floors * 3s/floor + 5s load = 20 ticks.
        awaitTrue(() -> elevator.getActiveLeg() != null, "elevator to start heading toward the pickup floor");
        ElevatorEvent pickupLeg = elevator.getActiveLeg();
        assertEquals(0, pickupLeg.getOriginFloor());
        assertEquals(5, pickupLeg.getDestination());
        assertEquals(0, pickupLeg.getDepartureTick());
        assertEquals(20, pickupLeg.getExpectedArrival(), "pickup leg should take 5 floors * 3s/floor + 5s load = 20 ticks");

        // Poll on the leg reference changing (the LAST write in the loading branch), so
        // currentFloor and the passenger count are guaranteed already updated by the time we
        // observe it - polling on currentFloor here instead would race ahead of the load.
        advanceClockTo(20);
        awaitTrue(() -> elevator.getActiveLeg() != null && elevator.getActiveLeg() != pickupLeg,
                "elevator to finish loading and start heading toward the dropoff floor");
        ElevatorEvent dropoffLeg = elevator.getActiveLeg();
        assertEquals(5, elevator.getCurrentFloor(), "elevator should be at the pickup floor once loading is complete");
        assertEquals(2, elevator.getCurrentPassengers(), "passengers should be loaded the instant the elevator arrives");

        // Dropoff leg: floor 5 -> floor 8, 3 floors away.
        // Expected duration = 3 floors * 3s/floor + 5s unload = 14 ticks, starting at tick 20.
        assertEquals(5, dropoffLeg.getOriginFloor());
        assertEquals(8, dropoffLeg.getDestination());
        assertEquals(20, dropoffLeg.getDepartureTick());
        assertEquals(34, dropoffLeg.getExpectedArrival(), "dropoff leg should take 3 floors * 3s/floor + 5s unload = 14 ticks after departure");

        // Poll on currentFloor changing (the LAST write in the unloading branch), so activeLeg
        // and the passenger count are guaranteed already updated by the time we observe it.
        advanceClockTo(34);
        awaitTrue(() -> elevator.getCurrentFloor() == 8, "elevator to arrive at the dropoff floor");
        assertEquals(0, elevator.getCurrentPassengers(), "passengers should be unloaded the instant the elevator arrives");
        assertNull(elevator.getActiveLeg(), "elevator should be idle again with no queued moves");
    }

    @Test
    void goingDownPickupAndDropoffTakeExactlyThreeSecondsPerFloorPlusFiveSecondsPerStop() {
        BuildingManager manager = new BuildingManager();
        Elevator elevator = startElevator(manager);

        // 3 passengers waiting at floor 7, heading to floor 2 (going down).
        manager.updatePassengerRequest(7, 2, 3);

        // Pickup leg: floor 0 -> floor 7, 7 floors away.
        // Expected duration = 7 floors * 3s/floor + 5s load = 26 ticks.
        awaitTrue(() -> elevator.getActiveLeg() != null, "elevator to start heading toward the pickup floor");
        ElevatorEvent pickupLeg = elevator.getActiveLeg();
        assertEquals(0, pickupLeg.getOriginFloor());
        assertEquals(7, pickupLeg.getDestination());
        assertEquals(0, pickupLeg.getDepartureTick());
        assertEquals(26, pickupLeg.getExpectedArrival(), "pickup leg should take 7 floors * 3s/floor + 5s load = 26 ticks");

        advanceClockTo(26);
        awaitTrue(() -> elevator.getActiveLeg() != null && elevator.getActiveLeg() != pickupLeg,
                "elevator to finish loading and start heading toward the dropoff floor");
        ElevatorEvent dropoffLeg = elevator.getActiveLeg();
        assertEquals(7, elevator.getCurrentFloor(), "elevator should be at the pickup floor once loading is complete");
        assertEquals(3, elevator.getCurrentPassengers());

        // Dropoff leg: floor 7 -> floor 2, 5 floors away.
        // Expected duration = 5 floors * 3s/floor + 5s unload = 20 ticks, starting at tick 26.
        assertEquals(7, dropoffLeg.getOriginFloor());
        assertEquals(2, dropoffLeg.getDestination());
        assertEquals(26, dropoffLeg.getDepartureTick());
        assertEquals(46, dropoffLeg.getExpectedArrival(), "dropoff leg should take 5 floors * 3s/floor + 5s unload = 20 ticks after departure");

        advanceClockTo(46);
        awaitTrue(() -> elevator.getCurrentFloor() == 2, "elevator to arrive at the dropoff floor");
        assertEquals(0, elevator.getCurrentPassengers());
        assertNull(elevator.getActiveLeg());
    }

    @Test
    void singleFloorHopTakesExactlyThreeSecondsPlusFiveSecondsPerStop() {
        BuildingManager manager = new BuildingManager();
        Elevator elevator = startElevator(manager);

        // 1 passenger waiting at floor 1, heading to floor 0 - the minimal 1-floor distance,
        // to confirm the 3s/floor rate isn't just coincidentally correct at larger distances.
        manager.updatePassengerRequest(1, 0, 1);

        awaitTrue(() -> elevator.getActiveLeg() != null, "elevator to start heading toward the pickup floor");
        ElevatorEvent pickupLeg = elevator.getActiveLeg();
        assertEquals(0, pickupLeg.getOriginFloor());
        assertEquals(1, pickupLeg.getDestination());
        assertEquals(8, pickupLeg.getExpectedArrival(), "1 floor * 3s/floor + 5s load = 8 ticks");

        advanceClockTo(8);
        awaitTrue(() -> elevator.getActiveLeg() != null && elevator.getActiveLeg() != pickupLeg,
                "elevator to finish loading and start heading toward the dropoff floor");
        ElevatorEvent dropoffLeg = elevator.getActiveLeg();
        assertEquals(1, elevator.getCurrentFloor(), "elevator should be at the pickup floor once loading is complete");
        assertEquals(1, elevator.getCurrentPassengers());

        assertEquals(1, dropoffLeg.getOriginFloor());
        assertEquals(0, dropoffLeg.getDestination());
        assertEquals(16, dropoffLeg.getExpectedArrival(), "1 floor * 3s/floor + 5s unload after departure at tick 8 = 16");

        advanceClockTo(16);
        awaitTrue(() -> elevator.getCurrentFloor() == 0, "elevator to arrive at the dropoff floor");
        assertEquals(0, elevator.getCurrentPassengers());
        assertNull(elevator.getActiveLeg());
    }

    @Test
    void elevatorRemainsIdleWithNoActiveLegBeforeAnyRequest() {
        BuildingManager manager = new BuildingManager();
        Elevator elevator = startElevator(manager);

        // Give the busy-spinning thread a brief moment to run its detecting branch a few
        // times with no requests present, then confirm it hasn't fabricated a move.
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertNull(elevator.getActiveLeg());
        assertEquals(0, elevator.getCurrentFloor());
        assertNotNull(manager); // manager stays usable/unclaimed with no requests
    }
}
