## Context

`BuildingManager.getCurrentRequest(floor)` and `Elevator.getCurrentPassengers()`
already reflect *post*-transfer counts the instant the GUI can observe
them — `BuildingManager.pickUpPassengers(floor)` zeroes a floor's pending
requests the same tick `Elevator.incPassengers()` increments the rider
count, and the unloading branch decrements `numPassengers` before the GUI
can poll it. There is no natural "in-flight" window in the domain model
to read a transfer's progress from. See proposal.md - Why for the
motivation.

## Goals / Non-Goals

**Goals:**
- Represent each individual waiting/riding passenger as a circle instead
  of a count/label.
- Animate a visible transfer of circles between a floor and an elevator
  car on pickup/dropoff, without rendering the same passengers twice
  (once "still arriving" and once "already resting").
- Keep changes to `Elevator` minimal, additive, and read-only, matching
  the existing `activeLeg` pattern from the original GUI change.

**Non-Goals:**
- Per-passenger identity/tracking across multiple trips — passengers are
  visually interchangeable circles, not individually identified.
- Rendering more than 12 simultaneously displayed circles per floor or
  per elevator; a config that produces bursts larger than that renders
  only the first 12 (no "+N" overflow indicator). Verified safe against
  the shipped sample `ElevatorConfig.txt`, whose passenger-count fields
  are all single digits.
- A physically accurate transfer duration; the 350ms transit duration is
  a fixed GUI-only artistic constant with no counterpart in the
  simulation's tick-based scheduling.

## Decisions

**One `PassengerTransfer` event per pickup/dropoff tick, not per
passenger.** All destinations loaded in a single tick in `Elevator.run()`
share the same origin floor and tick, so they are one indistinguishable
pickup event from the GUI's point of view. A batch of N passengers is
rendered as N circles moving together (via a deterministic grid offset at
render time), not N independently-timed transits. This keeps the
suppression bookkeeping (below) to one counter update per event instead
of N.

**Suppression counter, not a shared "already animating" set.** Since
`getCurrentPassengers()` jumps to the post-pickup value the instant a
transit is detected, naively rendering `currentPassengers` resting
circles *and* the incoming transit circles would double-count. A
per-elevator `pickupSuppression` counter (incremented when a pickup
transit spawns, decremented when it completes) lets the elevator's
rendered resting count be `max(0, currentPassengers - pickupSuppression)`
— simple, and self-correcting even if multiple pickup transits overlap.
Dropoff needs no equivalent: `getCurrentRequest(floor)` only counts
*pending pickups* and is never touched by a dropoff, so the floor side is
always correct on its own; dropoff transit circles are a purely
decorative overlay flying away from an already-correctly-reduced elevator
count.

**Change-detection by tick number, mirroring the existing
`lastObservedTick` pattern.** `PassengerTransitManager` spawns a new
transit only when a `PassengerTransfer`'s tick differs from the last one
seen for that shaft — the same "compare against last-seen, act only on
change" idiom `ElevatorGuiApp` already uses for `SimClock.getTime()`, so
readers of the codebase see one consistent idiom rather than two.

**`ElevatorCarView` switches from `StackPane` to `Pane`.** `StackPane`
auto-centers its children as a group, which fights explicit per-circle
grid positioning for a multi-circle layout. `Pane` (already
`BuildingView`'s style: every child positioned by explicit coordinates)
avoids `setManaged(false)` workarounds and keeps the two view classes
visually consistent.

**No `javafx.animation.Transition`/`Timeline`.** Consistent with
`ElevatorInterpolator`, `PassengerTransit`'s progress is a plain
`[0,1]`-clamped function of wall-clock time, recomputed every frame by
the existing single `AnimationTimer` — no second animation mechanism
introduced.

## Risks / Trade-offs

- [Risk] A missed intermediate tick between two `AnimationTimer` frames
  (if a pickup and a later pickup happened to land on the exact same
  polled frame) could cause a transit to be skipped. → Mitigation:
  `rateOfSimMs` is far larger than one frame in practice; this is the
  same accepted-simplification class as the existing `lastObservedTick`
  pattern it mirrors, not a new risk.
- [Risk] Circles beyond the 12-per-location cap are silently not
  rendered. → Mitigation: verified the shipped sample config never
  produces bursts anywhere near that size; documented as a non-goal.
- [Risk] No display server is available to visually verify the transfer
  animation in this environment. → Mitigation: same as the original GUI
  change — verify via compilation, unit tests on the pure-Java transit
  math, and a `mvn javafx:run` smoke test that resolves JavaFX/loads the
  app and fails only at `Unable to open DISPLAY`; visual confirmation is
  left to the user.

## Migration Plan

Not applicable — purely additive to the existing GUI, no deployment or
data migration involved.
