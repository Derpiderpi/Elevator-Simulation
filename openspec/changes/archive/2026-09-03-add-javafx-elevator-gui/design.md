## Context

The repository is currently a flat set of default-package `.java` files
with no build system (`javac`/`java` invocation only). `Elevator` runs on
its own busy-spinning `Thread` per car (3 total), coordinating through a
`synchronized`-method `BuildingManager` and a shared static `SimClock`
tick counter. Elevator position (`currentfloor`) is only updated at the
single simulated tick when a full travel-plus-dwell `ElevatorEvent`
completes — there is no existing notion of a car's position while it is
"in flight" between floors. See proposal.md - Why / What Changes for the
motivation and scope.

## Goals / Non-Goals

**Goals:**
- Animate elevator car position smoothly and continuously between floors
  in real time, without rewriting the simulation's scheduling logic.
- Keep changes to existing simulation classes minimal and read-only
  (getters and `volatile` qualifiers only) so simulation behavior and
  console output are unaffected.
- Keep the console-only entry point runnable unchanged.

**Non-Goals:**
- Fixing the pre-existing `Thread.sleep(totalSimTime)` call at the start
  of `ElevatorSimulation.start()` (an existing quirk, unrelated to this
  change).
- Perfectly still rendering during an elevator's dwell (load/unload) time
  — the chosen interpolation approach allows very slight visible creep
  during dwell; see Risks / Trade-offs.
- Live per-floor exit-count display, FXML-based views, or jlink/jpackage
  native packaging.
- Automated visual/screenshot testing of the rendered animation.

## Decisions

**Named package (`com.elevatorsim`), not the default package.**
`javafx-maven-plugin` and standard Maven project layout assume named
packages; keeping the default package is a known source of
classpath/launcher friction and gains nothing here. Alternative
considered: keep the default package to minimize diff size — rejected,
since the fix (adding one package line per file) is trivial while the
downside (launcher friction) is not.

**Plain classpath mode, no `module-info.java`.**
`javafx-maven-plugin`'s `javafx:run` goal works in classpath mode without
requiring the project to be modularized. Modularizing would only be
needed for `jlink`/`jpackage` native installers, which are out of scope.

**Poll from a single `javafx.animation.AnimationTimer`, not a push model.**
Continuous animation needs a per-frame position recompute regardless of
how state changes are communicated, so polling is not paying for anything
a push model would avoid. Retrofitting an observer/event mechanism into
`Elevator.run()`'s existing hot loop would touch working, already-correct
control flow; polling only adds new read-only getters, leaving every
existing line of scheduling logic untouched. Alternative considered:
`Platform.runLater` pushed from inside `Elevator.run()` on each state
change — rejected, since it still wouldn't produce smooth continuous
motion by itself (state changes are discrete ticks) and it risks
introducing bugs into the simulation's core loop.

**Cross-thread reads via `volatile`, not additional `synchronized`
blocks.** The GUI only ever *reads* elevator state; it never mutates it.
`volatile` on the handful of fields the GUI reads (`currentfloor`,
`numPassengers`, a new `activeLeg` reference, `SimClock.currentTime`,
`ElevatorSimulation.rateOfSim`) is sufficient for visibility across
threads and is far less invasive than wrapping `Elevator`'s hot loop in
synchronized blocks. `ElevatorEvent`'s new `originFloor`/`departureTick`
fields are `final`, which (combined with the containing object being
published only via a `volatile` reference and never mutated after
construction) guarantees safe publication of the whole object per JLS
17.5. `BuildingManager`/`BuildingFloor` need no changes since the GUI
reads through `BuildingManager`'s existing `synchronized` accessors.

**GUI owns the simulation's lifecycle; no callback/injection added to
`ElevatorSimulation`.** `ElevatorGuiApp.start(Stage)` constructs an
`ElevatorSimulation` and runs `sim.start()` on a background thread itself,
then polls its getters. This avoids adding any GUI-awareness to
`ElevatorSimulation`, keeping the console entry point (which constructs
and runs `ElevatorSimulation` the same way it always has) completely
unaffected.

**Self-calibrating interpolation clock, not a fixed "sim start"
timestamp.** `ElevatorSimulation.start()` currently calls
`Thread.sleep(totalSimTime)` immediately after starting the elevator
threads and before its tick loop begins (`ElevatorSimulation.java:46`).
Deriving elapsed simulated time from a single "sim started at time T0"
wall-clock timestamp would be thrown off by this pre-existing delay. The
interpolator instead has the GUI's animation timer track, locally, the
wall-clock timestamp of the last time it *observed* `SimClock.getTime()`
change, and computes progress from that observed reference point plus the
known ms-per-tick rate. This sidesteps the quirk entirely without
requiring any change to `ElevatorSimulation`'s timing behavior.

## Risks / Trade-offs

- [Risk] The interpolator computes progress across an event's whole
  `[departureTick, expectedArrival]` window, which includes trailing
  dwell (load/unload) time, so a car may appear to creep very slightly
  during dwell instead of sitting perfectly still. → Mitigation: accepted
  for v1; documented as a known simplification. A future change could
  expose a travel-only end tick to fix this without touching this
  change's scope.
- [Risk] No display server is available in the environment implementing
  this change, so the rendered animation cannot be visually verified
  here. → Mitigation: verify via compilation (`mvn compile`/`package`),
  unit tests on the pure-Java interpolation math, and a console-mode
  output diff before/after the source relocation; call out explicitly
  that visual smoothness needs local verification by the user.
- [Risk] Making `SimClock.currentTime` `volatile` changes a pre-existing
  latent visibility race (previously only read racily across the 3
  elevator threads) into a properly visible read. → Mitigation: this is
  a strict improvement (fixes a latent bug) and cannot change simulation
  behavior, since `volatile` only affects visibility timing, not values.

## Migration Plan

Not applicable — this is an additive change to a project with no
deployment/release process. Existing simulation behavior and output are
preserved; the only user-facing change is the new Maven-based build and
run instructions (documented in the updated README).
