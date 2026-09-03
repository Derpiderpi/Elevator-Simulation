## 1. Domain State Exposure

- [x] 1.1 Add `com.elevatorsim.PassengerTransfer` (immutable floor/count/tick) and verify `mvn -q compile`
- [x] 1.2 `Elevator`: add `volatile lastPickup`/`lastDropoff` fields + getters; accumulate `pickedUpThisTick` across the loading branch's up/down loops and assign `lastPickup` once after both loops when > 0; assign `lastDropoff` in the unloading branch when `arrivedPassengers` > 0; verify `mvn -q compile`
- [x] 1.3 Run the console-mode regression check (`mvn exec:java -Dexec.mainClass=com.elevatorsim.Simulator`) and confirm the output is structurally unchanged (ran cleanly, same format)

## 2. Transit Interpolation Math

- [x] 2.1 Add `com.elevatorsim.gui.PassengerTransit` (no `javafx.*` import): `Kind {PICKUP, DROPOFF}`, `DURATION_MS = 350`, `computeProgress(nowMs)`, `isComplete(nowMs)`
- [x] 2.2 Add `PassengerTransitTest` covering start/mid/end progress and clamping; verify `mvn -q test`
- [x] 2.3 Add `com.elevatorsim.gui.PassengerTransitManager` (spawn on tick-change via `observePickup`/`observeDropoff`, `pickupSuppression[]` increment on pickup spawn / decrement on pickup completion via `tick(nowMs)`, dropoff never touches suppression)
- [x] 2.4 Add `PassengerTransitManagerTest` covering: spawn-once-per-tick-change, suppression lifecycle across a pickup's spawn/complete, dropoff is a no-op on suppression, multiple concurrent transits across shafts; verify `mvn -q test` (21/21 pass across all three test classes)

## 3. Circle Rendering

- [x] 3.1 `BuildingView`: bump `LEFT_MARGIN` to 150; replace `Circle[] callIndicators` with a persistent `Circle[FLOOR_COUNT][12]` waiting-passenger grid (6 cols x 2 rows, radius 4, 11px spacing, origin x=70); replace `updateFloorCallState(int, boolean)` with `updateFloorWaitingCount(int floor, int count)`
- [x] 3.2 `ElevatorCarView`: switch from `StackPane` to `Pane`; position the body `Rectangle` explicitly at (0,0,CAR_WIDTH,CAR_HEIGHT); replace `passengerLabel` with a persistent `Circle[12]` riding-passenger grid (6x2, radius 5, 15px spacing, origin x=8,y=9); replace `updatePassengerCount(int)` with `updateRidingCount(int count)`
- [x] 3.3 Verify `mvn -q compile` succeeds with the reworked view classes

## 4. Transfer Animation Wiring

- [x] 4.1 `BuildingView`: add a `Group transitLayer` (added last, so it renders above floors/cars) and `updateTransits(List<PassengerTransit> active, long nowMs)` that computes each transit's interpolated pixel position from the existing floor-row-y / shaft-x formulas and rebuilds the transit layer's children each frame
- [x] 4.2 `ElevatorGuiApp`: instantiate one `PassengerTransitManager`; each frame call `observePickup`/`observeDropoff` per elevator from `getLastPickup()`/`getLastDropoff()`, then `transitManager.tick(nowMs)`; compute `visibleRiding = max(0, elevator.getCurrentPassengers() - transitManager.getPickupSuppression(i))` and pass it to `updateElevator`/`updateRidingCount` instead of the raw count; pass `manager.getCurrentRequest(floor)` to `updateFloorWaitingCount`; call `buildingView.updateTransits(...)` each frame
- [x] 4.3 Verify `mvn -q compile` and `mvn -q package` succeed

## 5. Docs & Verification

- [x] 5.1 Update `README.md` if it references the old label/dot behavior (added a sentence describing the circle/transfer-animation visualization)
- [x] 5.2 Re-run the console-mode regression check and confirm output is still structurally unchanged (ran cleanly via `mvn exec:java`)
- [x] 5.3 Run the full `mvn -q test` suite (existing `ElevatorInterpolatorTest` plus the new transit tests) — 21/21 pass
- [x] 5.4 Smoke-test `mvn javafx:run`: confirm it fails only at `Unable to open DISPLAY` after resolving JavaFX and loading `ElevatorGuiApp` (confirmed), and note that visually confirming circle movement requires a local display and is left to the user
