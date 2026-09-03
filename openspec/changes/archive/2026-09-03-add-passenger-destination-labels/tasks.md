## 1. Domain State Exposure

- [x] 1.1 `BuildingManager`: add `synchronized int[] getPassengerRequestsByDestination(int floor)` and verify `mvn -q compile`
- [x] 1.2 `Elevator`: add `volatile int[] ridingDestinationCounts` + `volatile int ridingOriginFloor`; reassign both at the pickup point (`ridingDestinationCounts = passengerDestinations.clone()`, `ridingOriginFloor = currentfloor`); zero the dropoff index into a fresh `ridingDestinationCounts` clone in the unloading branch (no update to `ridingOriginFloor` there); add `getRidingDestinationCounts()` (defensive clone) / `getRidingOriginFloor()` getters; verify `mvn -q compile`
- [x] 1.3 `PassengerTransfer`: replace `int count` with `int[] countsByDestination` (defensive clone in/out) plus a new `int originFloor`; keep `getCount()` as a derived sum; add `getOriginFloor()`; verify `mvn -q compile`
- [x] 1.4 `Elevator`: build `int[] pickedUpByDestination` inside the existing per-dest pickup loops (up/down); construct `int[] droppedByDestination` in the unloading branch; construct `lastPickup` with `originFloor = currentfloor`; construct `lastDropoff` with `originFloor = ridingOriginFloor`; verify `mvn -q compile`
- [x] 1.5 Run the console-mode regression check (`mvn exec:java -Dexec.mainClass=com.elevatorsim.Simulator`) and confirm output is structurally unchanged (ran cleanly)

## 2. Transit & Suppression Math

- [x] 2.1 `PassengerTransit`: replace `int count` with `int[] countsByDestination`; add `int originFloor`; keep `getCount()` derived; add `getOriginFloor()`; update constructor call sites
- [x] 2.2 `PassengerTransitManager`: change `pickupSuppression` from `int[shaftCount]` to `int[shaftCount][10]`, incrementing/decrementing per-destination in `observePickup`/`tick`; pass `pickup.getOriginFloor()`/`dropoff.getOriginFloor()` through to the `PassengerTransit` constructor; `getPickupSuppression(shaftIndex)` returns an `int[10]` clone
- [x] 2.3 Update `PassengerTransitTest` and `PassengerTransitManagerTest` for the new constructor/getter shapes (spawn-once-per-tick-change, per-destination suppression lifecycle across spawn/complete, dropoff never touches suppression, multiple concurrent transits across shafts/destinations, origin-floor propagation for both pickup and dropoff transits) (test compile still pending until `ElevatorGuiApp`/rendering classes are updated in task group 3-4, since they currently reference the old flat-count APIs)

## 3. Circle, Label & Color Rendering

- [x] 3.1 `BuildingView`: add `UP_COLOR = "#22C55E"`, `DOWN_COLOR = "#F97316"`, `PASSENGER_LABEL_COLOR = "#1F2937"`; bump `WAITING_CIRCLE_RADIUS` to 6, `WAITING_GRID_SPACING` to 14, `WAITING_GRID_X0` to 68; add persistent `Text[FLOOR_COUNT][MAX_WAITING_DISPLAY] waitingLabels`; replace `updateFloorWaitingCount(int, int)` with `updateFloorWaiting(int floor, int[] countsByDestination)` setting circle fill (direction color), label text (destination digit), and visibility per slot
- [x] 3.2 `ElevatorCarView`: bump `RIDING_CIRCLE_RADIUS` to 6, `RIDING_GRID_SPACING` to 14, `RIDING_GRID_Y0` to 8; add persistent `Text[MAX_RIDING_DISPLAY] passengerLabels`; replace `updateRidingCount(int)` with `updateRidingCounts(int[] countsByDestination, int originFloor)` referencing `BuildingView.UP_COLOR`/`DOWN_COLOR`/`PASSENGER_LABEL_COLOR`
- [x] 3.3 `BuildingView`: bump `TRANSIT_CIRCLE_RADIUS` to 6, `TRANSIT_FAN_SPACING` to 14; rework `updateTransits` to iterate `transit.getCountsByDestination()` per destination against `transit.getOriginFloor()`, creating a direction-colored `Circle` + digit `Text` per fanned sub-batch member
- [x] 3.4 Add a shared digit-label helper (`PassengerLabel` class; fixed-offset centering via `VPos.CENTER` + one empirical X nudge, no per-frame `getLayoutBounds()`), font size 9 bold, fill `PASSENGER_LABEL_COLOR`
- [x] 3.5 `BuildingView.updateElevator`: widened signature to `(int shaftIndex, double floorPosition, int[] ridingCountsByDestination, int ridingOriginFloor)`, delegating to `ElevatorCarView.updateRidingCounts`
- [x] 3.6 Verify `mvn -q compile` succeeds with the reworked view classes

## 4. Wiring

- [x] 4.1 `ElevatorGuiApp.render()`: compute per-destination `visibleRidingByDest` (`ridingCounts[d] - suppression[d]`, clamped at 0) plus `elevator.getRidingOriginFloor()`, pass both to `updateElevator`; replace `manager.getCurrentRequest(floor)` with `manager.getPassengerRequestsByDestination(floor)` passed to `updateFloorWaiting`
- [x] 4.2 Verify `mvn -q compile` and `mvn -q package` succeed

## 5. Docs & Verification

- [x] 5.1 Update `README.md` to mention destination-floor digit labels and direction-based coloring
- [x] 5.2 Re-run the console-mode regression check and confirm output is still structurally unchanged (ran cleanly)
- [x] 5.3 Run the full `mvn -q test` suite (existing tests plus updated transit/suppression tests) — 24/24 pass
- [x] 5.4 Smoke-test `mvn javafx:run`: confirm it fails only at `Unable to open DISPLAY` after resolving JavaFX and loading `ElevatorGuiApp` (confirmed); note that visually confirming legible digits and direction colors requires a local display and is left to the user
