# elevator-gui-animation Specification

## Purpose
Gives the elevator simulation a visual, animated JavaFX view of the
building so elevator movement, passenger loads, and floor call activity
can be observed in real time instead of only through console text output.

## Requirements

### Requirement: Building Visualization
The GUI SHALL render the building's 10 floors and 3 elevator shafts as a
3D scene, with each floor rendered as a stacked 3D slab and each shaft
rendered as a 3D column, all visible together from a single default
camera view when launched.

#### Scenario: GUI window opens
- **WHEN** the GUI application starts
- **THEN** 10 floor slabs and 3 shaft columns are rendered as 3D geometry
  and are visible together from the default camera position

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
The GUI SHALL display each elevator's current riding passengers as one
3D sphere per passenger, up to a maximum of 12 simultaneously displayed
spheres. Each sphere SHALL be labeled with a single digit denoting that
passenger's destination floor and colored according to whether that
passenger is traveling up or down relative to the floor where their
current ride began. The GUI SHALL update the displayed spheres when the
passenger count or destination breakdown changes.

#### Scenario: Passenger count changes
- **WHEN** an elevator's current passenger count changes
- **THEN** the number of rendered passenger spheres for that elevator
  updates to match the new count within one rendered frame, up to the
  12-sphere display maximum, each labeled with its destination floor
  digit and colored by direction of travel

#### Scenario: Passengers board without an in-progress transit
- **WHEN** an elevator's passenger count increases but no pickup transit
  animation is currently in progress for that elevator
- **THEN** the elevator's rendered sphere count reflects the full new
  passenger count immediately, labeled and colored as above

#### Scenario: Riding passengers traveling in different directions
- **WHEN** an elevator's current riders have destinations both above and
  below the floor where they boarded
- **THEN** the spheres for riders with a destination above the boarding
  floor and the spheres for riders with a destination below it are
  rendered in two visually distinct direction colors

### Requirement: Floor Call Indication
The GUI SHALL visually mark a floor's pending passenger pickup requests
as one 3D sphere per waiting passenger, up to a maximum of 12
simultaneously displayed spheres, rather than a single binary indicator.
Each sphere SHALL be labeled with a single digit denoting that
passenger's destination floor and colored according to whether that
destination is above or below the waiting floor.

#### Scenario: Floor has an outstanding request
- **WHEN** a floor has one or more pending passenger pickup requests
- **THEN** that floor renders one sphere per pending request, up to the
  12-sphere display maximum, each labeled with its destination floor
  digit and colored by direction of travel from that floor

#### Scenario: Floor's pending requests are fully served
- **WHEN** a floor's pending passenger pickup request count reaches zero
- **THEN** no waiting-passenger spheres are rendered for that floor

#### Scenario: Waiting passengers traveling in different directions
- **WHEN** a floor has waiting passengers with destinations both above
  and below that floor
- **THEN** the spheres for passengers heading up and the spheres for
  passengers heading down are rendered in two visually distinct
  direction colors

### Requirement: Console Mode Preserved
The system SHALL remain runnable as a console-only simulation, independent
of the GUI, with output unchanged from before this change.

#### Scenario: Console entry point runs without the GUI
- **WHEN** the console entry point (`com.elevatorsim.Simulator`) is run
  without launching the GUI
- **THEN** the simulation completes and prints the same console trace and
  summary output as before this change

### Requirement: Passenger Transfer Animation
The GUI SHALL animate passenger spheres moving through the 3D scene
between a floor and an elevator car when a pickup or dropoff occurs, over
a fixed real-time duration, rather than instantaneously appearing,
disappearing, or recounting at either location. Each traveling sphere
SHALL retain its destination-floor digit label and its direction color
throughout the animation.

#### Scenario: Passengers are picked up
- **WHEN** an elevator picks up passengers at a floor
- **THEN** spheres representing those passengers, each labeled with its
  destination floor digit and colored by direction of travel from the
  pickup floor, visibly travel from the floor's waiting position to the
  elevator car over a fixed animation duration, and the elevator's
  resting passenger-sphere count does not include those passengers until
  the travel animation completes

#### Scenario: Passengers are dropped off
- **WHEN** an elevator drops off passengers at a floor
- **THEN** spheres representing those passengers, each labeled with its
  destination floor digit and colored by the direction of travel from
  the floor where that ride began, visibly travel from the elevator car
  to the floor over a fixed animation duration before disappearing,
  without affecting the floor's waiting-passenger sphere count

#### Scenario: No double-counting during a pickup transit
- **WHEN** a pickup transit animation is in progress for an elevator
- **THEN** the total number of passenger spheres rendered at the floor's
  waiting position plus in transit plus resting in the elevator does not
  exceed the elevator's actual current passenger count at any rendered
  frame, and suppression of resting spheres is applied per destination
  so that a destination group with no in-progress transit is never
  reduced by a different destination's in-flight pickup

### Requirement: Camera Orbit and Zoom Controls
The GUI SHALL let the user orbit the camera around the building's
vertical axis by dragging the mouse within the building view, and zoom
the camera closer to or farther from the building using the scroll
wheel, without altering the position or state of any floor, shaft,
elevator, or passenger.

#### Scenario: User drags to orbit the view
- **WHEN** the user presses and drags the mouse within the building view
- **THEN** the camera's viewing angle around the building continuously
  follows the drag, and no floor, shaft, elevator, or passenger changes
  position or state as a result

#### Scenario: User scrolls to zoom
- **WHEN** the user scrolls the mouse wheel within the building view
- **THEN** the camera's distance from the building increases or
  decreases accordingly, remaining within a bounded range that keeps the
  building visible, and no floor, shaft, elevator, or passenger changes
  position or state as a result
