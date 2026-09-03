# elevator-gui-animation Specification

## Purpose
Gives the elevator simulation a visual, animated JavaFX view of the
building so elevator movement, passenger loads, and floor call activity
can be observed in real time instead of only through console text output.

## Requirements

### Requirement: Building Visualization
The GUI SHALL display the building's 10 floors and 3 elevator shafts as a
single visible layout when launched.

#### Scenario: GUI window opens
- **WHEN** the GUI application starts
- **THEN** 10 floor levels and 3 shaft columns are rendered in the window

### Requirement: Elevator Position Animation
The GUI SHALL animate an elevator's rendered position continuously between
its departure floor and its arrival floor, over a real-time duration
proportional to the simulated travel time for that leg, rather than
jumping instantly from one floor to the other.

#### Scenario: Elevator travels between floors
- **WHEN** an elevator begins a move from an origin floor to a destination
  floor
- **THEN** the elevator's rendered position progresses through
  intermediate points between the two floors over the course of the move,
  rather than jumping directly to the destination the instant it arrives

### Requirement: Idle Elevator Display
The GUI SHALL render an elevator with no active move as stationary at its
last-known floor.

#### Scenario: Elevator has no pending move
- **WHEN** an elevator has no active move event
- **THEN** the elevator is rendered fixed at its current floor, with no
  animation in progress

### Requirement: Passenger Load Display
The GUI SHALL display each elevator's current passenger count and update
the displayed value when the count changes.

#### Scenario: Passenger count changes
- **WHEN** an elevator's current passenger count changes
- **THEN** the displayed passenger count for that elevator updates to the
  new value within one rendered frame

### Requirement: Floor Call Indication
The GUI SHALL visually mark a floor that has a pending passenger pickup
request.

#### Scenario: Floor has an outstanding request
- **WHEN** a floor has one or more pending passenger pickup requests
- **THEN** that floor is rendered with a visible call-pending indicator

### Requirement: Console Mode Preserved
The system SHALL remain runnable as a console-only simulation, independent
of the GUI, with output unchanged from before this change.

#### Scenario: Console entry point runs without the GUI
- **WHEN** the console entry point (`com.elevatorsim.Simulator`) is run
  without launching the GUI
- **THEN** the simulation completes and prints the same console trace and
  summary output as before this change
