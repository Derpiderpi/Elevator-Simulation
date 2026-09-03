## 1. Build System

- [x] 1.1 Add `pom.xml` (Java 21 release, `org.openjfx:javafx-controls:21.0.4`, `javafx-maven-plugin` with mainClass `com.elevatorsim.gui.ElevatorGuiApp`, `exec-maven-plugin` for the console entry point) and verify `mvn -q help:effective-pom` resolves without error
- [x] 1.2 `git mv` the 8 existing source files into `src/main/java/com/elevatorsim/` and add `package com.elevatorsim;` to each, with no other line changes, and verify `mvn -q compile` succeeds
- [x] 1.3 Verify console-mode output is unchanged: run the simulation via `mvn exec:java -Dexec.mainClass=com.elevatorsim.Simulator` with a fixed `ElevatorConfig.txt` and confirm the trace matches a pre-migration baseline run (structurally identical format/sections; exact passenger totals are inherently non-deterministic even pre-migration, confirmed by running the unmodified baseline twice and observing differing totals from thread-scheduling races — not a regression)

## 2. Simulation State Exposure

- [x] 2.1 `ElevatorEvent`: add `final int originFloor` and `final int departureTick` fields, extend the constructor, add `getOriginFloor()`/`getDepartureTick()` getters, and update the 3 constructor call sites in `Elevator.run()` to pass the origin floor and current tick; verify `mvn -q compile` still succeeds
- [x] 2.2 `Elevator`: make `currentfloor` and `numPassengers` `volatile`, add a new `private volatile ElevatorEvent activeLeg` reassigned at each of the 3 `moveQueue` mutation points, and add `getCurrentFloor()`/`getActiveLeg()` getters; verify `mvn -q compile` succeeds
- [x] 2.3 `SimClock`: make `currentTime` `volatile`; verify `mvn -q compile` succeeds
- [x] 2.4 `ElevatorSimulation`: make `rateOfSim` `volatile` and add `getElevators()`/`getManager()`/`getRateOfSimMs()` getters; verify `mvn -q compile` succeeds
- [x] 2.5 Re-run the console-mode regression check from 1.3 after all state-exposure changes and confirm output is still unchanged (ran cleanly via `mvn exec:java`, same 3-elevator/10-floor output structure)

## 3. Interpolation Math

- [x] 3.1 Add `com.elevatorsim.gui.ElevatorInterpolator` (plain Java, no `javafx.*` imports) implementing the self-calibrating progress formula from design.md
- [x] 3.2 Add unit tests for `ElevatorInterpolator` covering: idle elevator (no active leg), mid-travel progress, tick-boundary edges, and clamping to [0,1]; verify `mvn -q test` passes (9/9 pass)

## 4. JavaFX GUI

- [x] 4.1 Add `com.elevatorsim.gui.ElevatorCarView` (`Region` subclass) with `updatePosition(double floorPosition)` and `updatePassengerCount(int n)`
- [x] 4.2 Add `com.elevatorsim.gui.BuildingView` (`Pane` subclass) rendering 10 floor rows x 3 shaft columns, floor call-pending indicators driven by `BuildingManager.getCurrentRequest(floor)`, and owning 3 `ElevatorCarView` children
- [x] 4.3 Add `com.elevatorsim.gui.ElevatorGuiApp` (`Application` subclass) whose `start(Stage)` constructs an `ElevatorSimulation`, runs it on a background thread, builds the `Scene`/`Stage` around a `BuildingView`, and drives a single `AnimationTimer` that recomputes each elevator's interpolated position/passenger count and each floor's call state every frame
- [x] 4.4 Guard the `AnimationTimer` against pre-`readFile()` state (skip/no-op frames while `getRateOfSimMs() == 0`)
- [x] 4.5 Verify `mvn -q compile` and `mvn -q package` succeed with the GUI classes included (both succeed; `mvn javafx:run` was also smoke-tested — it resolves the JavaFX runtime, loads `ElevatorGuiApp`, and fails only at `Unable to open DISPLAY`, confirming the app wiring itself is correct with no display server available)

## 5. Docs & Verification

- [x] 5.1 Update `README.md` with build/run instructions: `mvn exec:java -Dexec.mainClass=com.elevatorsim.Simulator` for console mode, `mvn javafx:run` for the GUI
- [x] 5.2 Verify `mvn -q package` succeeds for the full project
- [x] 5.3 Note in the change (and to the user) that actually launching `mvn javafx:run` and visually confirming smooth animation requires a local display and cannot be verified in this sandbox (confirmed: `mvn javafx:run` here fails only at `Unable to open DISPLAY`, after successfully resolving JavaFX and loading `ElevatorGuiApp`)
