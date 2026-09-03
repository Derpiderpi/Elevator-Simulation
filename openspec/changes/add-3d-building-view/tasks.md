## 1. Camera rig

- [ ] 1.1 Create `com.elevatorsim.gui.CameraController`: a JavaFX-free
  class holding yaw, pitch, and distance state, with methods to apply a
  mouse-drag delta (updating yaw/pitch, pitch clamped to avoid flipping
  past straight up/down) and a scroll delta (updating distance, clamped
  to a min/max range) — mirroring `ElevatorInterpolator`'s
  JavaFX-free, unit-testable style.
- [ ] 1.2 Add `src/test/java/com/elevatorsim/gui/CameraControllerTest.java`
  covering: yaw wraps/accumulates from repeated drags, pitch clamps at
  its bounds instead of flipping, and distance clamps at its min/max
  instead of zooming through or arbitrarily far away. Verify with
  `./mvnw test -Dtest=CameraControllerTest`.

## 2. 3D scene scaffold

- [ ] 2.1 Rework `BuildingView` to build a `Group` of 3D floor slabs
  (`Box`) and shaft columns (`Box`) at the same `floorY`/`shaftX` world
  coordinates the 2D version used, plus a `DEPTH` constant for their
  Z-extent, hosted in a `SubScene` with a `PerspectiveCamera`, an
  `AmbientLight`, and a `PointLight`. Verify by compiling
  (`./mvnw -q compile`) and confirming `BuildingView` no longer extends
  `Pane`/uses 2D `Line`/`Rectangle` for the floor/shaft layout.
- [ ] 2.2 Wire `CameraController` into the `SubScene`'s
  `setOnMousePressed`/`setOnMouseDragged`/`setOnScroll` handlers, applying
  its resulting yaw/pitch/distance to a camera-pivot `Group`'s `Rotate`
  transforms and the camera's Z translation each time a handler fires.
  Verify by compiling and by a manual read-through confirming no floor,
  shaft, elevator, or passenger node is touched by these handlers (only
  the camera pivot).
- [ ] 2.3 Add the 2D label-overlay `Pane` (transparent, mouse-transparent
  via `setMouseTransparent(true)` so drag/scroll still reach the
  `SubScene` beneath it) and a `StackPane` root containing the `SubScene`
  and the overlay, binding the `SubScene`'s width/height to the
  `StackPane`'s so the 3D view fills the window. Verify by compiling.

## 3. Elevator car in 3D

- [ ] 3.1 Rework `ElevatorCarView` (or its 3D replacement) to render the
  car body as a `Box` sized to the existing `CAR_WIDTH`/`CAR_HEIGHT` plus
  a car `DEPTH`, colored via `PhongMaterial` with the existing
  per-shaft `CAR_COLORS`, translated vertically by `updatePosition`
  using the same `floorY` formula the 2D car used. Verify by compiling
  and confirming `updatePosition`/`updateRidingCounts` keep their
  existing signatures so `ElevatorGuiApp` needs no call-site changes.
- [ ] 3.2 Rework the car's riding-passenger pool to `Sphere` +
  `PhongMaterial` (colored by `UP_COLOR`/`DOWN_COLOR`) in place of the
  `Circle` pool, keeping the fixed-size 12-slot array and
  visible/invisible toggling from `updateRidingCounts`. Verify by
  compiling.

## 4. Passenger spheres and digit labels

- [ ] 4.1 Rework `BuildingView`'s per-floor waiting-passenger pool from
  `Circle`+`Text` to `Sphere`+`Text`, positioning each sphere at its 3D
  waiting-grid position (reusing the existing grid-offset math, now with
  a `z` term) and keeping the digit `Text` in the 2D overlay layer.
  Verify by compiling.
- [ ] 4.2 Update `PassengerLabel` (or its call sites) so a label's screen
  position is recomputed every frame from its associated sphere's
  current 3D position via `Node.localToScreen(x, y, z)`, skipping that
  frame's repositioning if the result is `null` rather than throwing or
  leaving a stale position. Verify with a manual read-through of the
  null-guard, plus compilation.
- [ ] 4.3 Confirm the existing 12-passenger-per-location display cap,
  direction-based coloring (`UP_COLOR`/`DOWN_COLOR`), and destination
  digit labeling are preserved for both waiting and riding passengers by
  re-reading `updateFloorWaiting`/`updateRidingCounts` against the
  `elevator-gui-animation` delta spec's "Passenger Load Display" and
  "Floor Call Indication" requirements.

## 5. Transfer animation in 3D

- [ ] 5.1 Rework `BuildingView.updateTransits` to rebuild a 3D `Group` of
  `Sphere`+overlay-`Text` pairs per active `PassengerTransit`, position
  in 3D space each frame by interpolating from the floor's waiting
  position to the shaft's car position (or vice versa for dropoff) using
  `transit.computeProgress(nowMs)` exactly as the 2D version interpolated
  `fromX` to `toX`, with no change to `PassengerTransit` or
  `PassengerTransitManager`. Verify by compiling and by running
  `./mvnw test -Dtest=PassengerTransitTest,PassengerTransitManagerTest`
  to confirm those untouched classes still pass.

## 6. Wiring and console-mode check

- [ ] 6.1 Update `ElevatorGuiApp.start()` to build the new `StackPane`
  root (3D `SubScene` + overlay) instead of `new Scene(buildingView)`
  directly, keeping the simulation thread startup and `AnimationTimer`
  render loop unchanged. Verify by compiling.
- [ ] 6.2 Verify `com.elevatorsim.Simulator` (console-only entry point)
  still runs unaffected by running
  `./mvnw exec:java -Dexec.mainClass=com.elevatorsim.Simulator` against
  the shipped sample `ElevatorConfig.txt` and confirming console trace
  and summary output are unchanged from before this change.

## 7. Final verification

- [ ] 7.1 Run the full test suite (`./mvnw test`) and confirm all tests
  pass, including the new `CameraControllerTest` and the untouched
  `ElevatorInterpolatorTest`/`PassengerTransitTest`/
  `PassengerTransitManagerTest`/`ElevatorTimingTest`.
- [ ] 7.2 Run `./mvnw javafx:run` as a smoke test and confirm it resolves
  JavaFX and constructs the 3D scene, failing only at `Unable to open
  DISPLAY` (no display server in this environment) rather than at any
  compilation, class-loading, or scene-construction error. Document in
  the change's final summary that full visual/interactive confirmation
  (orbit, zoom, rendering correctness) is left to the user, consistent
  with prior GUI changes in this project.
