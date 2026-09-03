## Why

Passenger circles today are visually anonymous: an elevator's riding
circles are a uniform white, waiting circles are a uniform amber, and
none of them show where that passenger is actually headed. This makes it
hard to tell at a glance who's going where, or which direction a group
of passengers is traveling.

## What Changes

- Every passenger circle (waiting on a floor, riding in an elevator, or
  mid-transfer) is labeled with a single digit showing that passenger's
  destination floor.
- Every passenger circle is colored by travel direction — one color for
  passengers heading to a higher floor, another for a lower floor —
  replacing the previous context-based colors (amber for waiting, white
  for riding, shaft color for in-transit).
- Add minimal read-only state to `Elevator` and `PassengerTransfer`
  (mirroring the existing `activeLeg`/`lastPickup`/`lastDropoff` pattern)
  so the GUI can determine each passenger's destination and origin floor
  per circle, not just an aggregate count.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `elevator-gui-animation`: the "Passenger Load Display", "Floor Call
  Indication", and "Passenger Transfer Animation" requirements are
  extended so every rendered passenger circle carries a destination-floor
  digit label and a direction-based color.

## Impact

- `com.elevatorsim.Elevator`: two new read-only fields
  (`ridingDestinationCounts`, `ridingOriginFloor`) plus the pickup/dropoff
  loops build a per-destination breakdown instead of only an aggregate
  count; no change to existing scheduling logic or console output.
- `com.elevatorsim.PassengerTransfer`: reworked from a flat count to a
  per-destination breakdown plus an origin floor.
- `com.elevatorsim.BuildingManager`: one new read-only bulk getter for a
  floor's per-destination waiting counts.
- `com.elevatorsim.gui.PassengerTransit` / `PassengerTransitManager`:
  reworked to carry and suppress per-destination, not just an aggregate
  count.
- `com.elevatorsim.gui.BuildingView` / `ElevatorCarView`: render a digit
  label and direction color per circle instead of a flat count/color;
  circle size increases slightly for label legibility.
