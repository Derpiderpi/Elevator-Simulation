## Why

The simulation currently only reports elevator activity through console
`System.out.println` traces. There is no way to see elevator movement,
passenger loads, or floor call activity as they happen. A visual GUI that
animates elevator movement makes the simulation's behavior observable and
easier to understand at a glance.

## What Changes

- Adopt a Maven build (`pom.xml`) targeting Java 21, with the JavaFX
  `javafx-controls` dependency and the `javafx-maven-plugin` for running
  the GUI.
- Relocate the existing flat, default-package `.java` files into
  `src/main/java/com/elevatorsim/` under a real package, with no logic
  changes.
- Add a new JavaFX GUI (`com.elevatorsim.gui`) that renders the building's
  10 floors and 3 elevator shafts, and continuously animates each
  elevator's car position between its departure floor and arrival floor
  in real time (rather than jumping instantly on arrival).
- Add minimal read-only state exposure (getters, and `volatile`
  qualifiers for safe cross-thread reads) to `Elevator`, `ElevatorEvent`,
  `SimClock`, and `ElevatorSimulation` so the GUI can safely poll
  simulation state from the JavaFX Application Thread.
- Preserve the existing console-only simulation entry point
  (`com.elevatorsim.Simulator`) unchanged in behavior; it remains runnable
  without the GUI.
- **BREAKING**: running the project now requires Maven instead of plain
  `javac`/`java` invocation, since the source files move under
  `src/main/java/` and gain package declarations.

## Capabilities

### New Capabilities

- `elevator-gui-animation`: a JavaFX GUI that visually displays the
  building and animates elevator car movement in real time as the
  simulation runs.

### Modified Capabilities

(none — no existing capability specs exist in this repo yet)

## Impact

- All 8 existing source files (`Simulator`, `ElevatorSimulation`,
  `SimClock`, `Elevator`, `ElevatorEvent`, `BuildingManager`,
  `BuildingFloor`, `PassengerArrival`) are relocated to
  `src/main/java/com/elevatorsim/` and gain a `package com.elevatorsim;`
  declaration; no other lines change.
- New dependencies: JavaFX 21.0.4 (`javafx-controls`), Maven, the
  `javafx-maven-plugin`, and the `exec-maven-plugin` (to keep the console
  entry point runnable via Maven).
- New package `com.elevatorsim.gui` with the GUI application and view
  classes.
- `ElevatorConfig.txt` stays at the repository root and its format is
  unchanged.
