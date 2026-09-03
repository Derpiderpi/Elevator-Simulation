## Why

The GUI currently only shows a binary "call pending" dot per floor and a
numeric passenger-count label inside each elevator car. This is not very
visually clear — it doesn't show how many individual passengers are
waiting, and pickups/dropoffs happen as an instant count change with no
sense of passengers actually moving between the floor and the elevator.

## What Changes

- Replace the floor's binary call-pending indicator with one circle per
  waiting passenger.
- Replace the elevator's numeric passenger-count label with one circle
  per riding passenger.
- Animate passenger circles visibly traveling between a floor and an
  elevator car when a pickup or dropoff occurs, instead of the circle
  counts just changing instantly.
- Add minimal read-only state to `Elevator` (mirroring the existing
  `activeLeg` pattern) so the GUI can detect that a pickup/dropoff just
  happened, for triggering the transfer animation.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `elevator-gui-animation`: the "Passenger Load Display" and "Floor Call
  Indication" requirements change from a numeric label / binary indicator
  to per-passenger circles, and a new "Passenger Transfer Animation"
  requirement is added for the floor-to-elevator / elevator-to-floor
  transfer animation.

## Impact

- `com.elevatorsim.Elevator`: two new read-only `volatile` fields
  (`lastPickup`, `lastDropoff`) and a new small immutable
  `com.elevatorsim.PassengerTransfer` value class; no change to existing
  scheduling logic or console output.
- `com.elevatorsim.gui.BuildingView` and `com.elevatorsim.gui.ElevatorCarView`:
  reworked to render per-passenger circles instead of a label/dot.
- New `com.elevatorsim.gui.PassengerTransit` and
  `com.elevatorsim.gui.PassengerTransitManager` classes driving the
  transfer animation, following the existing pure-Java/unit-testable
  style of `ElevatorInterpolator`.
- `com.elevatorsim.gui.ElevatorGuiApp`: wires the new transit manager into
  its existing per-frame render loop.
