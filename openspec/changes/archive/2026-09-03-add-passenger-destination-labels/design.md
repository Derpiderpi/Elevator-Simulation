## Context

Every rendering path (floor waiting grid, elevator riding grid, transit
fan) currently tracks only a flat *count* of passengers, not which
destination each one is headed to. Labeling and coloring each circle
individually requires knowing, per circle, its passenger's destination
and the floor their trip began at. See proposal.md - Why for the
motivation.

## Goals / Non-Goals

**Goals:**
- Every rendered passenger circle carries a destination-floor digit and
  a direction (up/down) color, in all three contexts (waiting, riding,
  in transit).
- Keep the change additive and read-only against existing scheduling
  logic, mirroring the established `activeLeg`/`lastPickup`/`lastDropoff`
  safe-publication pattern.
- Keep the per-destination suppression bookkeeping correct — a
  destination group with no in-progress pickup transit must never lose
  circles to a different destination's in-flight suppression.

**Non-Goals:**
- Multi-digit destination labels — this project always has exactly 10
  floors (0-9), so every label is one digit; no overflow handling needed.
- Per-passenger identity across trips; passengers remain visually
  interchangeable within a destination/direction group.
- Growing `CAR_WIDTH`/`CAR_HEIGHT`/`FLOOR_HEIGHT` — the larger, labeled
  circles are sized to fit the existing dimensions with margin to spare
  (verified by arithmetic in the Decisions section).

## Decisions

**Move from flat counts to per-destination breakdowns everywhere, not a
lighter-weight workaround.** A flat scalar suppression counter
(`pickupSuppression: int[shaftCount]`, from the prior change) is only
correct when there's nothing destination-specific to get wrong. Once
rendering groups circles by destination, a same-shaft pickup transit for
one destination must not visually shrink a different, unrelated
destination's resting-circle group — which a flat scalar cannot express.
So `PassengerTransfer`/`PassengerTransit`/`pickupSuppression` all move to
per-destination (`int[10]` / `int[shaftCount][10]`). This is mostly
plumbing already-available domain data (`passengerDestinations`,
`BuildingFloor`'s per-destination arrays) through to the GUI, not new
bookkeeping invented from scratch.

**`Elevator.ridingOriginFloor`: one scalar per elevator, not per rider.**
Since a new pickup can only fire once `numPassengers` returns to 0 (an
existing invariant), every currently-onboard passenger boarded at the
same floor. A single `volatile int ridingOriginFloor`, reassigned only at
pickup, is therefore sufficient and stable for a batch's entire ride —
no need to track origin per rider.

**`PassengerTransfer` carries its own `originFloor`, rather than the GUI
deriving it separately.** For a pickup, origin equals the transfer's own
`floor`. For a dropoff, the transfer's `floor` is the *arrival* floor,
not the boarding floor, so origin must come from elsewhere — read from
`ridingOriginFloor` at construction time inside `Elevator.run()` (same
thread, no race) rather than having `ElevatorGuiApp` poll
`elevator.getRidingOriginFloor()` separately at GUI-consumption time,
which would decouple the origin reading from the specific transfer it
describes.

**Two fixed direction colors, one label color, no per-background contrast
logic.** `UP_COLOR = "#22C55E"` (green), `DOWN_COLOR = "#F97316"`
(orange) — a familiar directional convention, both bright/saturated
enough to read against the dark `#111827` background, and distinct
enough from `CAR_COLORS` (`#3B82F6`/`#EF4444`/`#10B981`, which now
exclusively mean "which shaft's car body") to avoid implying a
shaft/passenger association that doesn't exist. A single
`PASSENGER_LABEL_COLOR = "#1F2937"` (dark slate) reads clearly against
both fills, since they're similarly bright/mid-luminance (as the prior
amber fill already proved) — this replaces three separate
context-specific label-color constants from the prior change.

**Circle radius 6px / grid spacing 14px across all three contexts,
verified to still fit existing dimensions.** Riding grid (tightest
constraint, `CAR_WIDTH=96 x CAR_HEIGHT=36`): rightmost circle center =
`8 + 5*14 = 78`, right edge = `78+6 = 84 < 96`; bottom circle center =
`8 + 14 = 22`, bottom edge = `22+6 = 28 < 36`. Waiting grid (`70` to
`150`, nudged origin to `68`): rightmost edge = `68+5*14+6 = 144 < 150`.
No `CAR_WIDTH`/`CAR_HEIGHT`/`FLOOR_HEIGHT` change needed.

**Fixed-offset label centering, not per-frame `getLayoutBounds()`.**
Digits 0-9 in a bold font are near-uniform width, so a `Text` node with
`setTextOrigin(VPos.CENTER)` plus one empirical fixed X nudge avoids a
JavaFX layout-bounds measurement pass on every frame — relevant since the
transit layer already rebuilds its nodes from scratch every frame.

## Risks / Trade-offs

- [Risk] `PassengerTransfer`/`PassengerTransit` constructor signatures
  change (count → array, plus a new field), and the two existing test
  classes (`PassengerTransitTest`, `PassengerTransitManagerTest`)
  construct these directly. → Mitigation: update both test classes in
  the same change; no behavior they test is removed, only the
  construction shape changes.
- [Risk] No display server available to visually verify label legibility
  or color distinction. → Mitigation: same as every prior GUI change —
  verify via compilation, updated unit tests, and a `mvn javafx:run`
  smoke test that resolves JavaFX/loads the app and fails only at
  `Unable to open DISPLAY`; visual confirmation left to the user.

## Migration Plan

Not applicable — purely additive to the existing GUI, no deployment or
data migration involved.
